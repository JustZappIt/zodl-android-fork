package co.electriccoin.zcash.ui.screen.chat.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import co.electriccoin.zcash.ui.common.usecase.GetZashiAccountUseCase
import co.electriccoin.zcash.ui.common.usecase.NavigateToScanPublicKeyUseCase
import co.electriccoin.zcash.ui.screen.chat.media.FileUtils
import co.electriccoin.zcash.ui.screen.chat.media.ImageProcessor
import co.electriccoin.zcash.ui.screen.chat.model.ChatContact
import co.electriccoin.zcash.ui.screen.chat.model.ChatConversation
import co.electriccoin.zcash.ui.screen.chat.model.ChatIdentity
import co.electriccoin.zcash.ui.screen.chat.model.ChatMessage
import co.electriccoin.zcash.ui.screen.chat.model.ConversationType
import co.electriccoin.zcash.ui.screen.chat.model.MessageStatus
import org.json.JSONObject
import xyz.justzappit.zappmessaging.ZappMessagingSDK

/**
 * ViewModel for managing chat state and operations.
 * Ported from Zapp-Android — uses ZappMessagingSDK for P2P messaging.
 */
class ChatViewModel(
    application: Application,
    private val sdk: ZappMessagingSDK,
    private val navigateToScanPublicKey: NavigateToScanPublicKeyUseCase,
    private val getZashiAccount: GetZashiAccountUseCase
) : AndroidViewModel(application) {

    // ── Identity State ──────────────────────────────────────────────────

    private val _identity = MutableStateFlow<ChatIdentity?>(null)
    val identity: StateFlow<ChatIdentity?> = _identity.asStateFlow()

    // ── Conversation State ──────────────────────────────────────────────

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _currentConversation = MutableStateFlow<ChatConversation?>(null)
    val currentConversation: StateFlow<ChatConversation?> = _currentConversation.asStateFlow()

    // ── Message State ───────────────────────────────────────────────────

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // ── Contact State ───────────────────────────────────────────────────

    private val _contacts = MutableStateFlow<List<ChatContact>>(emptyList())
    val contacts: StateFlow<List<ChatContact>> = _contacts.asStateFlow()

    // ── Connection/UI State ─────────────────────────────────────────────

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _peerCount = MutableStateFlow(0)
    val peerCount: StateFlow<Int> = _peerCount.asStateFlow()

    private val _dhtHealth = MutableStateFlow(DhtHealth.HEALTHY)
    val dhtHealth: StateFlow<DhtHealth> = _dhtHealth.asStateFlow()

    private val _conversationPeerOnline = MutableStateFlow<Boolean?>(null)
    val conversationPeerOnline: StateFlow<Boolean?> = _conversationPeerOnline.asStateFlow()

    private val _connectionDetails = MutableStateFlow<ZappMessagingSDK.ConnectionDetails?>(null)
    val connectionDetails: StateFlow<ZappMessagingSDK.ConnectionDetails?> = _connectionDetails.asStateFlow()

    // Scan-result bridge: holds the last public key scanned via the QR scanner
    // destination. Lives in viewModelScope so it survives navigation to/from
    // the scanner screen; the UI consumes it via LaunchedEffect + consumeScannedKey().
    private val _scannedPublicKey = MutableStateFlow<String?>(null)
    val scannedPublicKey: StateFlow<String?> = _scannedPublicKey.asStateFlow()

    enum class ConnectionStatus {
        CONNECTED, CONNECTING, DISCONNECTED, ERROR
    }

    enum class DhtHealth {
        HEALTHY, DEGRADED, CRITICAL
    }

    // ── Unread Tracking ────────────────────────────────────────────────

    private val _unreadCounts = MutableStateFlow<Map<String, Int>>(emptyMap())

    val totalUnreadCount: StateFlow<Int> = _unreadCounts
        .combine(_conversations) { counts, _ -> counts.values.sum() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── Caching ─────────────────────────────────────────────────────────

    private var messagesCache = mutableMapOf<String, Pair<Long, List<ChatMessage>>>()
    private var contactsCacheTimestamp: Long = 0L
    private var conversationReloadJob: Job? = null
    private val autoAddedKeys = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    // ── Initialization ──────────────────────────────────────────────────

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            _connectionStatus.value = ConnectionStatus.CONNECTING
            try {
                sdk.initialize(getApplication())

                sdk.identity.value?.let { zmIdentity ->
                    _identity.value = ChatIdentity.from(zmIdentity)
                }

                suspendRefreshConversations()
                loadContacts()
                subscribeToSDKEvents()

                if (sdk.identity.value != null) {
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                }
                Log.i(TAG, "Chat system ready via SDK")
            } catch (e: Exception) {
                _connectionStatus.value = ConnectionStatus.ERROR
                _errorMessage.value = "Failed to initialize messaging: ${e.message}"
                Log.e(TAG, "Init failed", e)
            } finally {
                _isInitializing.value = false
            }
        }
    }

    private fun subscribeToSDKEvents() {
        // Incoming messages
        viewModelScope.launch {
            sdk.messageReceived.collect { (conversationId, zmMessage) ->
                val msg = ChatMessage.from(zmMessage)

                if (!zmMessage.isFromMe) {
                    autoAddUnknownSender(zmMessage.senderId, zmMessage.senderName)
                }

                val isViewingConversation = _currentConversation.value?.id == conversationId

                if (isViewingConversation) {
                    _messages.value = _messages.value + msg
                }
                messagesCache.remove(conversationId)

                if (!zmMessage.isFromMe && !isViewingConversation) {
                    val current = _unreadCounts.value.toMutableMap()
                    current[conversationId] = (current[conversationId] ?: 0) + 1
                    _unreadCounts.value = current
                }

                _conversations.value = _conversations.value.map {
                    if (it.id == conversationId) it.copy(
                        lastMessage = msg.content.ifEmpty { "[Media]" },
                        lastMessageTimestamp = msg.timestamp,
                        unreadCount = _unreadCounts.value[conversationId] ?: it.unreadCount
                    ) else it
                }
            }
        }

        // Conversation invites
        viewModelScope.launch {
            sdk.inviteReceived.collect {
                debouncedConversationReload()
            }
        }

        // Member left
        viewModelScope.launch {
            sdk.memberLeft.collect { (conversationId, leaverKey) ->
                _conversations.value = _conversations.value.map {
                    if (it.id == conversationId) it.copy(
                        participantIds = it.participantIds.filter { id -> id != leaverKey }
                    ) else it
                }
            }
        }

        // Group deleted
        viewModelScope.launch {
            sdk.groupDeleted.collect { conversationId ->
                _conversations.value = _conversations.value.filter { it.id != conversationId }
                if (_currentConversation.value?.id == conversationId) {
                    _currentConversation.value = null
                    _messages.value = emptyList()
                }
            }
        }

        // Group renamed
        viewModelScope.launch {
            sdk.groupRenamed.collect { (conversationId, newName) ->
                _conversations.value = _conversations.value.map {
                    if (it.id == conversationId) it.copy(displayName = newName) else it
                }
            }
        }

        // Member added
        viewModelScope.launch {
            sdk.memberAdded.collect { (conversationId, memberKey, memberName) ->
                autoAddUnknownSender(memberKey, memberName)
                _conversations.value = _conversations.value.map {
                    if (it.id == conversationId && memberKey !in it.participantIds) it.copy(
                        participantIds = it.participantIds + memberKey
                    ) else it
                }
            }
        }

        // Connection status
        viewModelScope.launch {
            sdk.isOnline.collect { online ->
                _connectionStatus.value = if (online)
                    ConnectionStatus.CONNECTED else ConnectionStatus.DISCONNECTED
            }
        }

        // Peer count
        viewModelScope.launch {
            sdk.peerCount.collect { count ->
                _peerCount.value = count
            }
        }

        // Message delivery status
        viewModelScope.launch {
            sdk.messageStatus.collect { (messageId, _, status) ->
                val mapped = when (status) {
                    "sent" -> MessageStatus.SENT
                    "queued" -> MessageStatus.QUEUED
                    "failed" -> MessageStatus.FAILED
                    else -> null
                }
                if (mapped != null) {
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == messageId) msg.copy(status = mapped) else msg
                    }
                }
            }
        }

        // Media download complete
        viewModelScope.launch {
            sdk.mediaDownloadComplete.collect { (mediaId, filePath) ->
                _messages.value = _messages.value.map { msg ->
                    if (msg.mediaId == mediaId && msg.mediaLocalPath == null) {
                        msg.copy(mediaLocalPath = filePath)
                    } else {
                        msg
                    }
                }
                _currentConversation.value?.id?.let { messagesCache.remove(it) }
            }
        }

        // DHT health
        viewModelScope.launch {
            sdk.dhtHealth.collect { health ->
                _dhtHealth.value = when (health) {
                    "degraded" -> DhtHealth.DEGRADED
                    "critical" -> DhtHealth.CRITICAL
                    else -> DhtHealth.HEALTHY
                }
            }
        }

        // Per-conversation peer status
        viewModelScope.launch {
            sdk.peerStatus.collect { (conversationId, _, status) ->
                if (conversationId == _currentConversation.value?.id) {
                    _conversationPeerOnline.value = status == "online"
                }
            }
        }
    }

    // ── Identity Management ─────────────────────────────────────────────

    fun createIdentity(displayName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val zmIdentity = sdk.createIdentity(displayName)
                _identity.value = ChatIdentity.from(zmIdentity)
                Log.i(TAG, "Identity created: ${zmIdentity.displayName}")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create identity: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restoreFromSeedPhrase(seedPhrase: String, displayName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val zmIdentity = sdk.restoreFromSeedPhrase(seedPhrase, displayName)
                _identity.value = ChatIdentity.from(zmIdentity)
                refreshConversations()
                loadContacts()
                Log.i(TAG, "Identity restored: ${zmIdentity.displayName}")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to restore identity: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDisplayName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        // SDK no longer exposes updateDisplayName; update locally only.
        _identity.value = _identity.value?.copy(displayName = trimmed)
    }

    fun deleteIdentity(onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                _identity.value = null
                _conversations.value = emptyList()
                _messages.value = emptyList()
                _contacts.value = emptyList()
                onDeleted()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete identity: ${e.message}"
            }
        }
    }

    fun exportSeedPhrase(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val seedPhrase = sdk.exportSeedPhrase()
                onResult(seedPhrase)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to export seed phrase: ${e.message}"
                onResult(null)
            }
        }
    }

    /**
     * Suspend variant used by Compose-driven flows (LaunchedEffect). Returns
     * the recovery phrase as a single space-delimited string, or null on
     * failure. Caller is responsible for not retaining the value longer than
     * needed — it's sensitive material.
     */
    suspend fun exportSeedPhraseSuspending(): String? = try {
        sdk.exportSeedPhrase()
    } catch (e: Exception) {
        _errorMessage.value = "Failed to export seed phrase: ${e.message}"
        null
    }

    // ── Conversation Management ─────────────────────────────────────────

    fun createDirectChat(
        publicKey: String,
        displayName: String? = null,
        onCreated: (conversationId: String) -> Unit = {}
    ) {
        val cleanedKey = publicKey.trim().removePrefix("0x")
        viewModelScope.launch {
            if (!isValidPublicKey(cleanedKey)) {
                _errorMessage.value = "Invalid public key - must be 64 hex characters"
                return@launch
            }
            _isLoading.value = true
            _currentConversation.value = null
            try {
                val zmConv = sdk.createConversation(
                    type = xyz.justzappit.zappmessaging.models.ConversationType.DIRECT,
                    participants = listOf(cleanedKey),
                    displayName = displayName
                )
                val conversation = ChatConversation.from(zmConv)
                if (_conversations.value.none { it.id == conversation.id }) {
                    _conversations.value = listOf(conversation) + _conversations.value
                }
                _currentConversation.value = conversation
                loadMessages(conversation.id)
                onCreated(conversation.id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create chat: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createGroupChat(participants: List<String>, displayName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _currentConversation.value = null
            try {
                val zmConv = sdk.createConversation(
                    type = xyz.justzappit.zappmessaging.models.ConversationType.GROUP,
                    participants = participants,
                    displayName = displayName
                )
                val conversation = ChatConversation.from(zmConv)
                if (_conversations.value.none { it.id == conversation.id }) {
                    _conversations.value = listOf(conversation) + _conversations.value
                }
                _currentConversation.value = conversation
                loadMessages(conversation.id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create group: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun leaveConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                sdk.removeConversation(conversationId)
                _conversations.value = _conversations.value.filter { it.id != conversationId }
                if (_currentConversation.value?.id == conversationId) {
                    _currentConversation.value = null
                    _messages.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to leave conversation: ${e.message}"
            }
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                sdk.deleteConversation(conversationId)
                _conversations.value = _conversations.value.filter { it.id != conversationId }
                if (_currentConversation.value?.id == conversationId) {
                    _currentConversation.value = null
                    _messages.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete conversation: ${e.message}"
            }
        }
    }

    fun renameGroup(conversationId: String, name: String) {
        viewModelScope.launch {
            try {
                sdk.renameGroup(conversationId, name)
                _conversations.value = _conversations.value.map {
                    if (it.id == conversationId) it.copy(displayName = name) else it
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to rename group: ${e.message}"
            }
        }
    }

    fun addMember(conversationId: String, publicKey: String) {
        viewModelScope.launch {
            try {
                if (!isValidPublicKey(publicKey)) {
                    _errorMessage.value = "Invalid public key format"
                    return@launch
                }
                sdk.addMember(conversationId, publicKey)
                _conversations.value = _conversations.value.map {
                    if (it.id == conversationId) it.copy(
                        participantIds = it.participantIds + publicKey
                    ) else it
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add member: ${e.message}"
            }
        }
    }

    fun refreshConversations() {
        viewModelScope.launch {
            suspendRefreshConversations()
        }
    }

    private suspend fun suspendRefreshConversations() {
        try {
            sdk.refreshConversations()
            val zmConversations = sdk.conversations.value
            val localCounts = _unreadCounts.value
            _conversations.value = zmConversations.map { zmConv ->
                ChatConversation.from(zmConv).let { conv ->
                    val localCount = localCounts[conv.id]
                    if (localCount != null && localCount > 0) conv.copy(unreadCount = localCount)
                    else conv
                }
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to refresh conversations: ${e.message}"
        }
    }

    fun setCurrentConversation(conversation: ChatConversation?) {
        _currentConversation.value = conversation
        _conversationPeerOnline.value = null
        conversation?.let { conv ->
            clearUnreadCount(conv.id)
            loadMessages(conv.id)
        }
    }

    private fun clearUnreadCount(conversationId: String) {
        if (_unreadCounts.value.containsKey(conversationId)) {
            val updated = _unreadCounts.value.toMutableMap()
            updated.remove(conversationId)
            _unreadCounts.value = updated
            _conversations.value = _conversations.value.map {
                if (it.id == conversationId) it.copy(unreadCount = 0) else it
            }
        }
    }

    // ── Message Management ──────────────────────────────────────────────

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            if (_currentConversation.value?.id != conversationId) {
                val match = _conversations.value.find { it.id == conversationId }
                if (match != null) {
                    _currentConversation.value = match
                } else {
                    try {
                        suspendRefreshConversations()
                        _currentConversation.value = _conversations.value.find { it.id == conversationId }
                    } catch (_: Exception) { }
                }
            }

            clearUnreadCount(conversationId)

            val cached = messagesCache[conversationId]
            if (cached != null && System.currentTimeMillis() - cached.first < MESSAGE_CACHE_TTL_MS) {
                _messages.value = cached.second
                return@launch
            }

            _isLoading.value = true
            try {
                val zmMessages = sdk.getMessages(conversationId)
                val msgs = zmMessages.map { ChatMessage.from(it) }
                messagesCache[conversationId] = Pair(System.currentTimeMillis(), msgs)
                _messages.value = msgs
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load messages: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Internal: push a local file over the SDK as a media/file message. */
    private fun sendMediaMessage(
        conversationId: String,
        mediaPath: String,
        contentType: String,
        caption: String = "",
        thumbnailData: String? = null
    ) {
        viewModelScope.launch {
            try {
                val zmMessage = sdk.sendMediaMessage(
                    conversationId,
                    mediaPath,
                    contentType,
                    caption,
                    thumbnailData
                )
                val message = ChatMessage.from(zmMessage)
                if (_currentConversation.value?.id == conversationId) {
                    _messages.value = _messages.value + message
                }
                messagesCache.remove(conversationId)
                val preview = mediaLastMessagePreview(contentType, caption)
                _conversations.value = _conversations.value.map {
                    if (it.id == conversationId) it.copy(
                        lastMessage = preview,
                        lastMessageTimestamp = message.timestamp
                    ) else it
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to send media: ${e.message}"
            }
        }
    }

    fun processAndSendMedia(conversationId: String, uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val mimeType = FileUtils.getMimeType(context, uri)
                val thumbnail = if (mimeType.startsWith("image/")) {
                    ImageProcessor.generateThumbnail(context, uri)
                } else null
                if (mimeType.startsWith("image/")) {
                    val compressed = ImageProcessor.compressImage(context, uri)
                        ?: throw IllegalStateException("Image compression failed")
                    sendMediaMessage(
                        conversationId,
                        compressed.absolutePath,
                        "image/jpeg",
                        thumbnailData = thumbnail
                    )
                } else {
                    val cached = FileUtils.copyUriToCache(context, uri)
                        ?: throw IllegalStateException("Failed to cache media")
                    sendMediaMessage(
                        conversationId,
                        cached.absolutePath,
                        mimeType,
                        thumbnailData = thumbnail
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to process media: ${e.message}"
            }
        }
    }

    fun processAndSendFile(conversationId: String, uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val cached = FileUtils.copyUriToCache(context, uri)
                    ?: throw IllegalStateException("Failed to cache file")
                val mimeType = FileUtils.getMimeType(context, uri)
                val fileName = FileUtils.getFileName(context, uri) ?: "File"
                val thumbnail = if (mimeType.startsWith("image/")) {
                    ImageProcessor.generateThumbnail(context, uri)
                } else null
                sendMediaMessage(
                    conversationId,
                    cached.absolutePath,
                    mimeType,
                    fileName,
                    thumbnailData = thumbnail
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to process file: ${e.message}"
            }
        }
    }

    fun processAndSendCameraCapture(conversationId: String, uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val thumbnail = ImageProcessor.generateThumbnail(context, uri)
                val compressed = ImageProcessor.compressImage(context, uri)
                    ?: throw IllegalStateException("Image compression failed")
                sendMediaMessage(
                    conversationId,
                    compressed.absolutePath,
                    "image/jpeg",
                    thumbnailData = thumbnail
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to process photo: ${e.message}"
            }
        }
    }

    fun sendLocationMessage(
        conversationId: String,
        latitude: Double,
        longitude: Double,
        accuracy: Float
    ) {
        viewModelScope.launch {
            try {
                val content = JSONObject().apply {
                    put("latitude", latitude)
                    put("longitude", longitude)
                    put("accuracy", accuracy.toDouble())
                }.toString()
                val zmMessage = sdk.sendMessage(conversationId, content, "application/location")
                val message = ChatMessage.from(zmMessage)
                if (_currentConversation.value?.id == conversationId) {
                    _messages.value = _messages.value + message
                }
                messagesCache.remove(conversationId)
                _conversations.value = _conversations.value.map {
                    if (it.id == conversationId) it.copy(
                        lastMessage = "\uD83D\uDCCD Location",
                        lastMessageTimestamp = message.timestamp
                    ) else it
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to share location: ${e.message}"
            }
        }
    }

    /** Resolve the user's own unified address and share it into the conversation. */
    fun shareMyAddress(conversationId: String) {
        viewModelScope.launch {
            try {
                val account = getZashiAccount()
                val address = account.unified.address.address
                shareWalletAddressInternal(conversationId, address)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get wallet address: ${e.message}"
            }
        }
    }

    private suspend fun shareWalletAddressInternal(conversationId: String, address: String) {
        try {
            val zmMessage = sdk.sendMessage(conversationId, address, "application/wallet-address")
            val message = ChatMessage.from(zmMessage)
            if (_currentConversation.value?.id == conversationId) {
                _messages.value = _messages.value + message
            }
            messagesCache.remove(conversationId)
            _conversations.value = _conversations.value.map {
                if (it.id == conversationId) it.copy(
                    lastMessage = "\uD83D\uDD17 Address",
                    lastMessageTimestamp = message.timestamp
                ) else it
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to share wallet address: ${e.message}"
        }
    }

    fun shareWalletAddress(conversationId: String, address: String) {
        viewModelScope.launch { shareWalletAddressInternal(conversationId, address) }
    }

    private fun mediaLastMessagePreview(contentType: String, caption: String): String {
        if (caption.isNotBlank()) return caption
        return when {
            contentType.startsWith("image/") -> "\uD83D\uDCF7 Photo"
            contentType.startsWith("video/") -> "\uD83C\uDFA5 Video"
            contentType.startsWith("audio/") -> "\uD83C\uDFB5 Audio"
            else -> "\uD83D\uDCC4 File"
        }
    }

    fun sendMessage(conversationId: String, content: String) {
        viewModelScope.launch {
            try {
                val zmMessage = sdk.sendMessage(conversationId, content)
                val message = ChatMessage.from(zmMessage)
                if (_currentConversation.value?.id == conversationId) {
                    _messages.value = _messages.value + message
                }
                messagesCache.remove(conversationId)
                _conversations.value = _conversations.value.map {
                    if (it.id == conversationId) it.copy(
                        lastMessage = content,
                        lastMessageTimestamp = message.timestamp
                    ) else it
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to send message: ${e.message}"
            }
        }
    }

    // ── Contact Management ──────────────────────────────────────────────

    fun loadContacts() {
        viewModelScope.launch {
            if (System.currentTimeMillis() - contactsCacheTimestamp < CONTACTS_CACHE_TTL_MS) {
                return@launch
            }
            try {
                sdk.refreshContacts()
                val zmContacts = sdk.contacts.value
                _contacts.value = zmContacts.map { ChatContact.from(it) }
                contactsCacheTimestamp = System.currentTimeMillis()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load contacts: ${e.message}"
            }
        }
    }

    fun addContact(publicKey: String, name: String) {
        val cleanedKey = publicKey.trim().removePrefix("0x")
        viewModelScope.launch {
            try {
                sdk.addContact(cleanedKey, name)
                contactsCacheTimestamp = 0L
                loadContacts()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add contact: ${e.message}"
            }
        }
    }

    fun updateContact(publicKey: String, name: String) {
        viewModelScope.launch {
            try {
                sdk.updateContact(publicKey, name)
                contactsCacheTimestamp = 0L
                loadContacts()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update contact: ${e.message}"
            }
        }
    }

    fun deleteContact(publicKey: String) {
        viewModelScope.launch {
            try {
                sdk.deleteContact(publicKey)
                contactsCacheTimestamp = 0L
                loadContacts()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete contact: ${e.message}"
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    fun clearError() {
        _errorMessage.value = null
    }

    /** Fetch detailed connection info from the SDK for diagnostics display. */
    fun fetchConnectionDetails() {
        viewModelScope.launch {
            try {
                _connectionDetails.value = sdk.getConnectionDetails()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch connection details: ${e.message}")
            }
        }
    }

    // ── Scan bridge ─────────────────────────────────────────────────────

    fun scanPublicKey() {
        viewModelScope.launch {
            navigateToScanPublicKey()?.let { key ->
                _scannedPublicKey.value = key
            }
        }
    }

    fun consumeScannedKey() {
        _scannedPublicKey.value = null
    }

    private fun autoAddUnknownSender(senderId: String, senderName: String?) {
        val name = senderName?.takeIf { it.isNotBlank() } ?: return
        if (!isValidPublicKey(senderId)) return
        if (_contacts.value.any { it.publicKey == senderId }) return
        if (!autoAddedKeys.add(senderId)) return

        viewModelScope.launch {
            try {
                sdk.addContact(senderId, name)
                contactsCacheTimestamp = 0L
                loadContacts()
            } catch (e: Exception) {
                autoAddedKeys.remove(senderId)
                Log.w(TAG, "Auto-add contact failed: ${e.message}")
            }
        }
    }

    private fun debouncedConversationReload() {
        conversationReloadJob?.cancel()
        conversationReloadJob = viewModelScope.launch {
            delay(CONVERSATION_RELOAD_DEBOUNCE_MS)
            try {
                suspendRefreshConversations()
            } catch (e: Exception) {
                Log.w(TAG, "Background conversation refresh failed: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        conversationReloadJob?.cancel()
    }

    private fun isValidPublicKey(key: String): Boolean {
        val cleaned = key.trim().removePrefix("0x")
        if (cleaned.length != PUBLIC_KEY_HEX_LENGTH) return false
        return cleaned.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }

    companion object {
        private const val TAG = "ChatViewModel"
        private const val MESSAGE_CACHE_TTL_MS = 60_000L
        private const val CONTACTS_CACHE_TTL_MS = 300_000L
        private const val CONVERSATION_RELOAD_DEBOUNCE_MS = 500L
        private const val PUBLIC_KEY_HEX_LENGTH = 64
    }
}
