package co.electriccoin.zcash.ui.screen.chat.view

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.electriccoin.zcash.ui.design.component.zapp.ZappBottomActionBar
import co.electriccoin.zcash.ui.design.component.zapp.ZappButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.component.zapp.initialsOf
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.screen.chat.model.ChatContact
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel

private data class SelectedParticipant(
    val publicKey: String,
    val displayName: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewConversationView(
    onConversationCreated: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {
    val c = ZappTheme.colors
    val contacts by viewModel.contacts.collectAsState()
    val scannedPublicKey by viewModel.scannedPublicKey.collectAsState()

    var searchInput by remember { mutableStateOf(TextFieldValue("")) }
    var groupNameInput by remember { mutableStateOf(TextFieldValue("")) }
    var selectedParticipants by remember { mutableStateOf<List<SelectedParticipant>>(emptyList()) }
    var isCreating by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(scannedPublicKey) {
        scannedPublicKey?.let { key ->
            searchInput = TextFieldValue(key)
            viewModel.consumeScannedKey()
        }
    }

    val searchText = searchInput.text.trim()
    val cleanedSearch = searchText.removePrefix("0x")
    val isPublicKey = cleanedSearch.length == 64
            && cleanedSearch.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }

    val filteredContacts = if (searchText.isEmpty()) contacts
    else contacts.filter {
        it.name.contains(searchText, ignoreCase = true) ||
                it.publicKey.contains(searchText, ignoreCase = true)
    }

    val canStartChat = selectedParticipants.isNotEmpty() && !isCreating

    Scaffold(
        modifier = modifier,
        topBar = {
            ZappScreenHeader(title = "New Conversation")
        },
        bottomBar = {
            ZappBottomActionBar(
                onBack = onNavigateBack,
                primaryAction = if (canStartChat) {
                    {
                        ZappButton(
                            text = if (selectedParticipants.size == 1) "Start Chat"
                                   else "Create Group (${selectedParticipants.size})",
                            onClick = {
                                if (isCreating) return@ZappButton
                                isCreating = true
                                if (selectedParticipants.size == 1) {
                                    viewModel.createDirectChat(
                                        publicKey = selectedParticipants.first().publicKey,
                                        displayName = selectedParticipants.first().displayName,
                                        onCreated = { conversationId ->
                                            isCreating = false
                                            onConversationCreated(conversationId)
                                        },
                                    )
                                } else {
                                    val groupName = groupNameInput.text.trim().ifEmpty {
                                        selectedParticipants.take(3).joinToString(", ") { it.displayName }
                                    }
                                    viewModel.createGroupChat(
                                        participants = selectedParticipants.map { it.publicKey },
                                        displayName = groupName,
                                    )
                                    isCreating = false
                                    onNavigateBack()
                                }
                            },
                        )
                    }
                } else null,
            )
        },
        containerColor = c.bg,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            // Selected participant chips
            if (selectedParticipants.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    selectedParticipants.forEach { participant ->
                        InputChip(
                            selected = true,
                            onClick = {
                                selectedParticipants = selectedParticipants.filter {
                                    it.publicKey != participant.publicKey
                                }
                            },
                            label = { Text(participant.displayName) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Group name (when 2+ participants)
            if (selectedParticipants.size >= 2) {
                OutlinedTextField(
                    value = groupNameInput,
                    onValueChange = { groupNameInput = it },
                    label = { Text("Group name (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Search field
            TextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search contacts or paste public key") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = c.textMuted)
                },
                trailingIcon = {
                    if (searchInput.text.isNotEmpty()) {
                        IconButton(onClick = { searchInput = TextFieldValue("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = c.textMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RectangleShape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = c.surfaceInput,
                    unfocusedContainerColor = c.surfaceInput,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = c.text,
                    unfocusedTextColor = c.text,
                    cursorColor = c.accent,
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick actions: scan + paste
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionChip(icon = Icons.Default.QrCodeScanner, label = "Scan QR") {
                    viewModel.scanPublicKey()
                }
                QuickActionChip(icon = Icons.Default.ContentPaste, label = "Paste") {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.primaryClip?.getItemAt(0)?.text?.let { text ->
                        searchInput = TextFieldValue(text.toString().trim())
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Public key detected banner
            if (isPublicKey && selectedParticipants.none { it.publicKey == cleanedSearch }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.accentSoft, RectangleShape)
                        .border(
                            androidx.compose.foundation.BorderStroke(1.dp, c.border),
                            RectangleShape,
                        )
                        .clickable {
                            val existingContact = contacts.find { it.publicKey == cleanedSearch }
                            selectedParticipants = selectedParticipants + SelectedParticipant(
                                publicKey = cleanedSearch,
                                displayName = existingContact?.name ?: "${cleanedSearch.take(8)}...",
                            )
                            searchInput = TextFieldValue("")
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = c.accentText,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            "Public key detected",
                            style = ZappTheme.typography.caption.copy(color = c.accentText),
                        )
                        BasicText(
                            "${cleanedSearch.take(12)}...${cleanedSearch.takeLast(6)}",
                            style = ZappTheme.typography.mono.copy(color = c.accentText),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    BasicText(
                        "Add",
                        style = ZappTheme.typography.button.copy(color = c.accentText),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Contact list
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(
                    items = filteredContacts.sortedBy { it.name.lowercase() },
                    key = { it.publicKey },
                ) { contact ->
                    val isSelected = selectedParticipants.any { it.publicKey == contact.publicKey }
                    ContactSelectRow(
                        contact = contact,
                        isSelected = isSelected,
                        onToggle = {
                            selectedParticipants = if (isSelected) {
                                selectedParticipants.filter { it.publicKey != contact.publicKey }
                            } else {
                                selectedParticipants + SelectedParticipant(
                                    publicKey = contact.publicKey,
                                    displayName = contact.name,
                                )
                            }
                        },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = c.border,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactSelectRow(
    contact: ChatContact,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    val c = ZappTheme.colors
    val initials = remember(contact.name) { initialsOf(contact.name) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(c.accent, RectangleShape),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                initials,
                style = ZappTheme.typography.rowTitle.copy(color = c.onAccent),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                contact.name,
                style = ZappTheme.typography.rowTitle.copy(color = c.text),
            )
            BasicText(
                "${contact.publicKey.take(8)}...${contact.publicKey.takeLast(4)}",
                style = ZappTheme.typography.mono.copy(color = c.textMuted),
            )
        }

        if (isSelected) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = c.accent,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val c = ZappTheme.colors
    Row(
        modifier = Modifier
            .background(c.surfaceAlt, RectangleShape)
            .border(androidx.compose.foundation.BorderStroke(1.dp, c.border), RectangleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = c.accent,
            modifier = Modifier.size(16.dp),
        )
        BasicText(label, style = ZappTheme.typography.caption.copy(color = c.text))
    }
}
