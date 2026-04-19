package co.electriccoin.zcash.ui.screen.chat.view

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import co.electriccoin.zcash.ui.screen.chat.media.rememberCameraCaptureState
import co.electriccoin.zcash.ui.screen.chat.model.ChatMessage
import co.electriccoin.zcash.ui.screen.chat.model.MessageStatus
import co.electriccoin.zcash.ui.screen.chat.view.bubbles.FileBubble
import co.electriccoin.zcash.ui.screen.chat.view.bubbles.LocationBubble
import co.electriccoin.zcash.ui.screen.chat.view.bubbles.MediaBubble
import co.electriccoin.zcash.ui.screen.chat.view.bubbles.PaymentRequestBubble
import co.electriccoin.zcash.ui.screen.chat.view.bubbles.TransactionBubble
import co.electriccoin.zcash.ui.screen.chat.view.bubbles.WalletAddressBubble
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomView(
    conversationId: String,
    onNavigateBack: () -> Unit,
    onSendZec: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val currentConversation by viewModel.currentConversation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val peerCount by viewModel.peerCount.collectAsState()
    val dhtHealth by viewModel.dhtHealth.collectAsState()
    val conversationPeerOnline by viewModel.conversationPeerOnline.collectAsState()

    var messageInput by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showMediaSheet by remember { mutableStateOf(false) }
    var showNetworkSheet by remember { mutableStateOf(false) }
    val connectionDetails by viewModel.connectionDetails.collectAsState()

    // Activity result launchers
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.processAndSendMedia(conversationId, it) }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.processAndSendFile(conversationId, it) }
    }

    val cameraCaptureState = rememberCameraCaptureState(context) { uri ->
        viewModel.processAndSendCameraCapture(conversationId, uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraCaptureState.launch()
        else Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            coroutineScope.launch { shareLocation(context, viewModel, conversationId) }
        } else {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch { listState.animateScrollToItem(messages.size - 1) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            currentConversation?.displayName ?: "Chat",
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium
                        )
                        val subtitleColor = when (connectionStatus) {
                            ChatViewModel.ConnectionStatus.CONNECTED -> when {
                                conversationPeerOnline == true -> MaterialTheme.colorScheme.primary
                                dhtHealth == ChatViewModel.DhtHealth.CRITICAL -> MaterialTheme.colorScheme.error
                                peerCount > 0 -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.tertiary
                            }
                            ChatViewModel.ConnectionStatus.CONNECTING -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                        val subtitle = when (connectionStatus) {
                            ChatViewModel.ConnectionStatus.CONNECTED -> when {
                                conversationPeerOnline == true -> "Peer online"
                                dhtHealth == ChatViewModel.DhtHealth.CRITICAL -> "DHT unreachable"
                                conversationPeerOnline == false -> "Peer offline - messages will queue"
                                peerCount > 0 -> "P2P connected"
                                dhtHealth == ChatViewModel.DhtHealth.DEGRADED -> "DHT degraded - no peers"
                                else -> "Waiting for peer..."
                            }
                            ChatViewModel.ConnectionStatus.CONNECTING -> "Connecting..."
                            ChatViewModel.ConnectionStatus.DISCONNECTED -> "Offline"
                            ChatViewModel.ConnectionStatus.ERROR -> "Connection error"
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ConnectionPill(
                        connectionStatus = connectionStatus,
                        peerCount = peerCount,
                        dhtHealth = dhtHealth,
                        onClick = {
                            viewModel.fetchConnectionDetails()
                            showNetworkSheet = true
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isLoading && messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                items(items = messages, key = { it.id }) { message ->
                    MessageBubble(message = message)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = { showAttachmentSheet = true },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Attach", modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 36.dp),
                    placeholder = { Text("Message") },
                    maxLines = 4,
                    shape = RoundedCornerShape(0.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = {
                        val text = messageInput.text.trim()
                        if (text.isNotEmpty()) {
                            viewModel.sendMessage(conversationId, text)
                            messageInput = TextFieldValue("")
                        }
                    },
                    enabled = messageInput.text.isNotBlank(),
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = "Send",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showAttachmentSheet) {
        AttachmentSheet(
            onShareAddress = {
                showAttachmentSheet = false
                viewModel.shareMyAddress(conversationId)
            },
            onSendZec = {
                showAttachmentSheet = false
                onSendZec()
            },
            onAttachMedia = {
                showAttachmentSheet = false
                showMediaSheet = true
            },
            onDismiss = { showAttachmentSheet = false }
        )
    }

    if (showNetworkSheet) {
        NetworkDetailsSheet(
            connectionStatus = connectionStatus,
            peerCount = peerCount,
            dhtHealth = dhtHealth,
            connectionDetails = connectionDetails,
            onDismiss = { showNetworkSheet = false }
        )
    }

    if (showMediaSheet) {
        MediaAttachmentSheet(
            onChooseMedia = {
                showMediaSheet = false
                mediaPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            },
            onAttachFile = {
                showMediaSheet = false
                filePickerLauncher.launch(arrayOf("*/*"))
            },
            onTakePhoto = {
                showMediaSheet = false
                val hasPerm = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPerm) cameraCaptureState.launch()
                else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onShareLocation = {
                showMediaSheet = false
                val hasPerm = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPerm) {
                    coroutineScope.launch { shareLocation(context, viewModel, conversationId) }
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            onDismiss = { showMediaSheet = false }
        )
    }
}

@Suppress("MissingPermission")
private suspend fun shareLocation(
    context: android.content.Context,
    viewModel: ChatViewModel,
    conversationId: String
) {
    try {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()
        val location = suspendCancellableCoroutine<android.location.Location?> { cont ->
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
            cont.invokeOnCancellation { cts.cancel() }
        }
        if (location != null) {
            viewModel.sendLocationMessage(
                conversationId,
                location.latitude,
                location.longitude,
                location.accuracy
            )
        } else {
            Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Location error: ${e.message ?: ""}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isFromMe = message.isFromMe
    val contentType = run {
        val declared = message.contentType
        if (!declared.isNullOrEmpty() && declared != "text/plain") {
            declared
        } else {
            try { JSONObject(message.content).optString("contentType", "").takeIf { it.isNotEmpty() } }
            catch (_: Exception) { null }
        } ?: "text/plain"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        if (!isFromMe && message.senderName != null) {
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        when {
            contentType == "application/payment-request" ->
                PaymentRequestBubble(message = message, isFromMe = isFromMe)
            contentType == "application/wallet-address" ->
                WalletAddressBubble(message = message, isFromMe = isFromMe)
            contentType == "application/zec-transaction" ->
                TransactionBubble(message = message, isFromMe = isFromMe)
            contentType == "application/location" ->
                LocationBubble(message = message, isFromMe = isFromMe)
            contentType.startsWith("image/") ->
                MediaBubble(message = message, isFromMe = isFromMe)
            contentType.startsWith("video/") ->
                MediaBubble(message = message, isFromMe = isFromMe)
            message.mediaId != null ->
                FileBubble(message = message, isFromMe = isFromMe)
            else ->
                TextMessageBubble(message = message, isFromMe = isFromMe)
        }
    }
}

@Composable
private fun TextMessageBubble(message: ChatMessage, isFromMe: Boolean) {
    Surface(
        shape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = if (isFromMe) 0.dp else 4.dp,
            bottomEnd = if (isFromMe) 4.dp else 0.dp
        ),
        color = if (isFromMe) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.widthIn(max = 280.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isFromMe) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatMessageTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFromMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isFromMe) {
                    DeliveryIndicator(
                        status = message.status,
                        tintColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryIndicator(status: MessageStatus?, tintColor: Color) {
    when (status) {
        MessageStatus.SENT -> Icon(
            Icons.Default.Done,
            contentDescription = "Sent",
            modifier = Modifier.size(13.dp),
            tint = tintColor
        )
        MessageStatus.QUEUED -> Icon(
            Icons.Default.Schedule,
            contentDescription = "Queued",
            modifier = Modifier.size(13.dp),
            tint = tintColor
        )
        MessageStatus.FAILED -> Icon(
            Icons.Default.ErrorOutline,
            contentDescription = "Failed",
            modifier = Modifier.size(13.dp),
            tint = MaterialTheme.colorScheme.error
        )
        MessageStatus.SENDING -> Icon(
            Icons.Default.MoreHoriz,
            contentDescription = "Sending",
            modifier = Modifier.size(13.dp),
            tint = tintColor
        )
        null -> {}
    }
}

private fun formatMessageTime(epochMillis: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(epochMillis))
