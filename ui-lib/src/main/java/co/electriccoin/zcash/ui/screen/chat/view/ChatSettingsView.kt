package co.electriccoin.zcash.ui.screen.chat.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.electriccoin.zcash.ui.design.component.zapp.ZappBottomActionBar
import co.electriccoin.zcash.ui.design.component.zapp.ZappButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappButtonVariant
import co.electriccoin.zcash.ui.design.component.zapp.ZappGroupHeader
import co.electriccoin.zcash.ui.design.component.zapp.ZappRow
import co.electriccoin.zcash.ui.design.component.zapp.ZappRowDivider
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.component.zapp.initialsOf
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatSettingsView(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onIdentityDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {
    val c = ZappTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val identity by viewModel.identity.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val peerCount by viewModel.peerCount.collectAsState()
    val dhtHealth by viewModel.dhtHealth.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    var showCopiedFeedback by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            ZappScreenHeader(title = "Chat Settings")
        },
        bottomBar = {
            ZappBottomActionBar(
                onBack = onNavigateBack,
                primaryAction = {
                    ZappButton(
                        text = "Delete Identity",
                        variant = ZappButtonVariant.Danger,
                        onClick = { showDeleteConfirm = true },
                    )
                },
            )
        },
        containerColor = c.bg,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Identity card
            identity?.let { id ->
                val initials = remember(id.displayName) { initialsOf(id.displayName) }

                ZappGroupHeader(text = "Identity")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.surface),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Square avatar
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(c.accent, RectangleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                initials,
                                style = ZappTheme.typography.sectionTitle.copy(color = c.onAccent),
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            BasicText(
                                id.displayName,
                                style = ZappTheme.typography.sectionTitle.copy(color = c.text),
                            )
                            IconButton(
                                onClick = { editNameText = id.displayName; showEditNameDialog = true },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit name",
                                    tint = c.textMuted,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Copyable public key
                        Row(
                            modifier = Modifier
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Public Key", id.publicKey))
                                    showCopiedFeedback = true
                                    scope.launch { delay(2000); showCopiedFeedback = false }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText(
                                "${id.publicKey.take(10)}...${id.publicKey.takeLast(6)}",
                                style = ZappTheme.typography.mono.copy(color = c.textMuted),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                if (showCopiedFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (showCopiedFeedback) c.success else c.textMuted,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
                ZappRowDivider()
            }

            // Account section
            ZappGroupHeader(text = "Account")
            ZappRow(title = "Profile", subtitle = "View identity details", onClick = onNavigateToProfile)
            ZappRowDivider(inset = true)
            ZappRow(title = "Contacts", subtitle = "Manage your contacts", onClick = onNavigateToContacts)
            ZappRowDivider()

            // Network section
            ZappGroupHeader(text = "Network")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.surface)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NetworkInfoRow(
                        label = "Connection",
                        value = when (connectionStatus) {
                            ChatViewModel.ConnectionStatus.CONNECTED -> "Connected"
                            ChatViewModel.ConnectionStatus.CONNECTING -> "Connecting"
                            ChatViewModel.ConnectionStatus.DISCONNECTED -> "Disconnected"
                            ChatViewModel.ConnectionStatus.ERROR -> "Error"
                        },
                        valueColor = when (connectionStatus) {
                            ChatViewModel.ConnectionStatus.CONNECTED -> c.success
                            ChatViewModel.ConnectionStatus.CONNECTING -> c.accent
                            else -> c.danger
                        },
                    )
                    NetworkInfoRow(
                        label = "DHT Health",
                        value = when (dhtHealth) {
                            ChatViewModel.DhtHealth.HEALTHY -> "Healthy"
                            ChatViewModel.DhtHealth.DEGRADED -> "Degraded"
                            ChatViewModel.DhtHealth.CRITICAL -> "Critical"
                        },
                        valueColor = when (dhtHealth) {
                            ChatViewModel.DhtHealth.HEALTHY -> c.success
                            ChatViewModel.DhtHealth.DEGRADED -> c.accent
                            ChatViewModel.DhtHealth.CRITICAL -> c.danger
                        },
                    )
                    NetworkInfoRow(
                        label = "Peers",
                        value = peerCount.toString(),
                        valueColor = if (peerCount > 0) c.success else c.textMuted,
                    )
                    NetworkInfoRow(label = "Protocol", value = "Hyperswarm DHT", valueColor = c.textMuted)
                    NetworkInfoRow(label = "Encryption", value = "Ed25519 + Noise", valueColor = c.textMuted)
                }
            }
            ZappRowDivider()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Display Name") },
            text = {
                OutlinedTextField(
                    value = editNameText,
                    onValueChange = { editNameText = it },
                    label = { Text("Display name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editNameText.isNotBlank()) {
                            viewModel.updateDisplayName(editNameText)
                            showEditNameDialog = false
                        }
                    },
                    enabled = editNameText.isNotBlank(),
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Identity") },
            text = { Text("This will permanently remove your messaging identity. Back up your seed phrase first.") },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false; viewModel.deleteIdentity { onIdentityDeleted() } },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun NetworkInfoRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    val c = ZappTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(label, style = ZappTheme.typography.body.copy(color = c.textMuted))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(modifier = Modifier.size(6.dp).background(valueColor, RectangleShape))
            BasicText(value, style = ZappTheme.typography.body.copy(color = valueColor))
        }
    }
}
