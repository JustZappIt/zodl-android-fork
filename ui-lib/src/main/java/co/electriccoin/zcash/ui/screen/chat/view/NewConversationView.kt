package co.electriccoin.zcash.ui.screen.chat.view

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.component.zapp.initialsOf
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.screen.chat.model.ChatContact
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel

private data class SelectedParticipant(
    val publicKey: String,
    val displayName: String,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewConversationView(
    onConversationCreated: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
) {
    val c = ZappTheme.colors
    val contacts by viewModel.contacts.collectAsState()
    val scannedPublicKey by viewModel.scannedPublicKey.collectAsState()

    var searchInput by remember { mutableStateOf(TextFieldValue("")) }
    var selectedParticipants by remember { mutableStateOf<List<SelectedParticipant>>(emptyList()) }
    var isCreating by remember { mutableStateOf(false) }

    LaunchedEffect(scannedPublicKey) {
        scannedPublicKey?.let { key ->
            searchInput = TextFieldValue(key)
            viewModel.consumeScannedKey()
        }
    }

    val searchText = searchInput.text.trim()
    val cleanedSearch = searchText.removePrefix("0x")
    val isPublicKey = cleanedSearch.length == 64 &&
        cleanedSearch.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }

    val filteredContacts = if (searchText.isEmpty()) contacts
    else contacts.filter {
        it.name.contains(searchText, ignoreCase = true) ||
            it.publicKey.contains(searchText, ignoreCase = true)
    }

    val canStartChat = selectedParticipants.isNotEmpty() && !isCreating
    val showEmptyState = searchInput.text.isEmpty() && selectedParticipants.isEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .imePadding(),
    ) {
        ZappScreenHeader(title = "New Conversation")

        // Body — grows to fill available space
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (showEmptyState) {
                EmptyState(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 28.dp),
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                ) {
                    // Selected participant chips
                    if (selectedParticipants.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            selectedParticipants.forEach { participant ->
                                Row(
                                    modifier = Modifier
                                        .background(c.accentSoft, RectangleShape)
                                        .border(BorderStroke(1.dp, c.border), RectangleShape)
                                        .clickable(onClick = {
                                            selectedParticipants = selectedParticipants
                                                .filter { it.publicKey != participant.publicKey }
                                        })
                                        .semantics {
                                            contentDescription = "Remove ${participant.displayName}"
                                            role = Role.Button
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    BasicText(
                                        text = participant.displayName,
                                        style = ZappTheme.typography.chip.copy(color = c.accentText),
                                    )
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = c.accentText,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Public key detected banner
                    if (isPublicKey && selectedParticipants.none { it.publicKey == cleanedSearch }) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(c.accentSoft, RectangleShape)
                                .border(BorderStroke(1.dp, c.border), RectangleShape)
                                .clickable {
                                    val existingContact = contacts.find { it.publicKey == cleanedSearch }
                                    selectedParticipants = selectedParticipants + SelectedParticipant(
                                        publicKey = cleanedSearch,
                                        displayName = existingContact?.name
                                            ?: "${cleanedSearch.take(8)}...",
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
                            Spacer(Modifier.width(12.dp))
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
                        Spacer(Modifier.height(12.dp))
                    }

                    // Filtered contact list
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 56.dp)
                                    .height(1.dp)
                                    .background(c.border, RectangleShape),
                            )
                        }
                    }
                }
            }
        }

        // Search / paste field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .background(c.surfaceInput, RectangleShape)
                .border(
                    BorderStroke(
                        width = if (searchInput.text.isNotEmpty()) 2.dp else 1.dp,
                        color = if (searchInput.text.isNotEmpty()) c.borderStrong else c.border,
                    ),
                    RectangleShape,
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 0.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = c.textSubtle,
                )
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        singleLine = true,
                        textStyle = ZappTheme.typography.body.copy(color = c.text),
                        cursorBrush = SolidColor(c.accent),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (searchInput.text.isEmpty()) {
                        BasicText(
                            text = "Type or paste a public key…",
                            style = ZappTheme.typography.body.copy(color = c.textSubtle),
                        )
                    }
                }
                if (searchInput.text.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { searchInput = TextFieldValue("") }
                            .semantics { contentDescription = "Clear"; role = Role.Button },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = c.textSubtle,
                        )
                    }
                }
            }
        }

        // Bottom dock: back ← + Scan QR Code / Start Chat
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.surface)
                .border(BorderStroke(1.dp, c.border), RectangleShape)
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            Box(
                modifier = Modifier
                    .size(width = 72.dp, height = 52.dp)
                    .border(BorderStroke(1.dp, c.border), RectangleShape)
                    .clickable(onClick = onNavigateBack)
                    .semantics { contentDescription = "Go back"; role = Role.Button },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    "←",
                    style = ZappTheme.typography.button.copy(
                        color = c.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .background(c.accent, RectangleShape)
                    .clickable(onClick = {
                        if (canStartChat) {
                            if (isCreating) return@clickable
                            isCreating = true
                            viewModel.createDirectChat(
                                publicKey = selectedParticipants.first().publicKey,
                                displayName = selectedParticipants.first().displayName,
                                onCreated = { conversationId ->
                                    isCreating = false
                                    onConversationCreated(conversationId)
                                },
                            )
                        } else {
                            viewModel.scanPublicKey()
                        }
                    })
                    .semantics {
                        contentDescription = if (canStartChat) "Start Chat" else "Scan QR Code"
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (!canStartChat) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = c.onAccent,
                        )
                    }
                    BasicText(
                        text = if (canStartChat) "START CHAT" else "SCAN QR CODE",
                        style = ZappTheme.typography.button.copy(
                            color = c.onAccent,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    val c = ZappTheme.colors
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Illustration box
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(c.surfaceAlt, RectangleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Chat,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = c.textSubtle,
            )
        }

        Spacer(Modifier.height(24.dp))

        BasicText(
            text = "Start a private conversation",
            style = ZappTheme.typography.sectionTitle.copy(
                color = c.text,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            ),
        )

        Spacer(Modifier.height(8.dp))

        BasicText(
            text = "Scan someone's QR code or enter their public key to send them a message.",
            style = ZappTheme.typography.body.copy(
                color = c.textMuted,
                textAlign = TextAlign.Center,
            ),
        )

        Spacer(Modifier.height(20.dp))

        // Privacy callout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.accentSoft, RectangleShape)
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(c.accent, RectangleShape),
            )
            Spacer(Modifier.width(12.dp))
            BasicText(
                text = "Messages are peer-to-peer encrypted and only readable by you and the recipient.",
                style = ZappTheme.typography.body.copy(color = c.accentText),
                modifier = Modifier.weight(1f),
            )
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
            .padding(horizontal = 18.dp, vertical = 12.dp),
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

        Spacer(Modifier.width(12.dp))

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
