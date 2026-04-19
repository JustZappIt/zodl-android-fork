package co.electriccoin.zcash.ui.screen.chat.view

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import co.electriccoin.zcash.ui.screen.chat.model.ChatContact
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel

private data class SelectedParticipant(
    val publicKey: String,
    val displayName: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewConversationView(
    onConversationCreated: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {
    val contacts by viewModel.contacts.collectAsState()
    val identity by viewModel.identity.collectAsState()
    val scannedPublicKey by viewModel.scannedPublicKey.collectAsState()

    var searchInput by remember { mutableStateOf(TextFieldValue("")) }
    var groupNameInput by remember { mutableStateOf(TextFieldValue("")) }
    var selectedParticipants by remember { mutableStateOf<List<SelectedParticipant>>(emptyList()) }
    var isCreating by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // QR scan runs on the ViewModel's scope so it survives this screen being
    // removed from composition while the scanner is open. When the result
    // lands, drop it into the search field and clear the VM state.
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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("New Conversation") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Selected participants chips
            if (selectedParticipants.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                            }
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
                    shape = RoundedCornerShape(0.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Search field
            TextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search contacts or paste public key") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchInput.text.isNotEmpty()) {
                        IconButton(onClick = { searchInput = TextFieldValue("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(0.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick actions: scan + paste
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionChip(
                    icon = Icons.Default.QrCodeScanner,
                    label = "Scan QR"
                ) {
                    viewModel.scanPublicKey()
                }
                QuickActionChip(
                    icon = Icons.Default.ContentPaste,
                    label = "Paste"
                ) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.primaryClip?.getItemAt(0)?.text?.let { text ->
                        searchInput = TextFieldValue(text.toString().trim())
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Public key detected
            if (isPublicKey && selectedParticipants.none { it.publicKey == cleanedSearch }) {
                Surface(
                    shape = RoundedCornerShape(0.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                val existingContact = contacts.find { it.publicKey == cleanedSearch }
                                selectedParticipants = selectedParticipants + SelectedParticipant(
                                    publicKey = cleanedSearch,
                                    displayName = existingContact?.name ?: "${cleanedSearch.take(8)}..."
                                )
                                searchInput = TextFieldValue("")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Public key detected", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${cleanedSearch.take(12)}...${cleanedSearch.takeLast(6)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            "Add",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Contact list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(
                    items = filteredContacts.sortedBy { it.name.lowercase() },
                    key = { it.publicKey }
                ) { contact ->
                    val isSelected = selectedParticipants.any { it.publicKey == contact.publicKey }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedParticipants = if (isSelected) {
                                    selectedParticipants.filter { it.publicKey != contact.publicKey }
                                } else {
                                    selectedParticipants + SelectedParticipant(
                                        publicKey = contact.publicKey,
                                        displayName = contact.name
                                    )
                                }
                            }
                            .padding(horizontal = 4.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                contact.name.take(2).uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(contact.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${contact.publicKey.take(8)}...${contact.publicKey.takeLast(4)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }

            // Start chat button
            if (selectedParticipants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (isCreating) return@Button
                        isCreating = true
                        if (selectedParticipants.size == 1) {
                            viewModel.createDirectChat(
                                publicKey = selectedParticipants.first().publicKey,
                                displayName = selectedParticipants.first().displayName,
                                onCreated = { conversationId ->
                                    isCreating = false
                                    onConversationCreated(conversationId)
                                }
                            )
                        } else {
                            val groupName = groupNameInput.text.trim().ifEmpty {
                                selectedParticipants.take(3).joinToString(", ") { it.displayName }
                            }
                            viewModel.createGroupChat(
                                participants = selectedParticipants.map { it.publicKey },
                                displayName = groupName
                            )
                            // For group chats, navigate back and refresh
                            isCreating = false
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isCreating,
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(
                        if (selectedParticipants.size == 1) "Start Chat"
                        else "Create Group (${selectedParticipants.size} members)"
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
