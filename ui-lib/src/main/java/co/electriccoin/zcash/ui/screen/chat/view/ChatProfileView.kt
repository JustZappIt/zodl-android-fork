package co.electriccoin.zcash.ui.screen.chat.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import co.electriccoin.zcash.ui.design.component.QrState
import co.electriccoin.zcash.ui.design.component.ZashiQr
import co.electriccoin.zcash.ui.design.component.zapp.ZappBottomActionBar
import co.electriccoin.zcash.ui.design.component.zapp.ZappButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappButtonVariant
import co.electriccoin.zcash.ui.design.component.zapp.ZappRow
import co.electriccoin.zcash.ui.design.component.zapp.ZappRowDivider
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.component.zapp.initialsOf
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatProfileView(
    onNavigateBack: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onIdentityDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {
    val c = ZappTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val identity by viewModel.identity.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCopiedFeedback by remember { mutableStateOf(false) }
    var showSeedPhrase by remember { mutableStateOf(false) }
    var seedPhrase by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            ZappScreenHeader(title = "Profile")
        },
        bottomBar = {
            ZappBottomActionBar(
                onBack = onNavigateBack,
                primaryAction = {
                    ZappButton(
                        text = "Delete Identity",
                        variant = ZappButtonVariant.Danger,
                        leadingIcon = Icons.Default.Delete,
                        onClick = { showDeleteDialog = true },
                    )
                },
            )
        },
        containerColor = c.bg,
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Square avatar with initials (Swiss-minimalist: no circles)
            val initials = remember(identity?.displayName) {
                identity?.displayName?.let { initialsOf(it) } ?: "?"
            }
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(c.accent, RectangleShape),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = initials,
                    style = ZappTheme.typography.displaySecondary.copy(color = c.onAccent),
                )
            }

            BasicText(
                identity?.displayName ?: "Unknown",
                style = ZappTheme.typography.sectionTitle.copy(color = c.text),
            )

            // QR code of public key
            identity?.publicKey?.let { pk ->
                ZashiQr(
                    state = QrState(qrData = pk),
                    modifier = Modifier.fillMaxWidth(),
                    qrSize = 200.dp,
                )

                // Public key card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.surfaceAlt, RectangleShape)
                        .border(
                            androidx.compose.foundation.BorderStroke(1.dp, c.border),
                            RectangleShape,
                        ),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                "Public Key",
                                style = ZappTheme.typography.caption.copy(color = c.textMuted),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            BasicText(
                                pk,
                                style = ZappTheme.typography.mono.copy(color = c.text),
                                maxLines = 3,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Public Key", pk))
                            showCopiedFeedback = true
                            scope.launch { delay(2000); showCopiedFeedback = false }
                        }) {
                            Icon(
                                if (showCopiedFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (showCopiedFeedback) c.success else c.textMuted,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Menu items using ZappRow
            ZappRow(
                title = "Seed Phrase",
                subtitle = "View your recovery words",
                icon = Icons.Default.Key,
                iconBackground = c.accentSoft,
                iconTint = c.accentText,
                onClick = {
                    viewModel.exportSeedPhrase { phrase ->
                        seedPhrase = phrase
                        showSeedPhrase = true
                    }
                },
            )
            ZappRowDivider(inset = true)
            ZappRow(
                title = "Contacts",
                subtitle = "Manage your contacts",
                icon = Icons.Default.Contacts,
                iconBackground = c.surfaceAlt,
                onClick = onNavigateToContacts,
            )
            ZappRowDivider(inset = true)
            ZappRow(
                title = "Network Status",
                subtitle = "P2P connection info",
                icon = Icons.Default.Settings,
                iconBackground = c.surfaceAlt,
                onClick = {},
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Identity") },
            text = { Text("This will remove your messaging identity. Make sure you've backed up your seed phrase. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteIdentity { onIdentityDeleted() }
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showSeedPhrase && seedPhrase != null) {
        AlertDialog(
            onDismissRequest = { showSeedPhrase = false },
            title = { Text("Seed Phrase") },
            text = {
                val words = seedPhrase!!.split(" ").filter { it.isNotBlank() }
                Column {
                    Text("Keep this safe. Anyone with these words can access your messaging identity.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(words.take(12) to 0, words.drop(12) to 12).forEach { (col, offset) ->
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                col.forEachIndexed { i, word ->
                                    Text(
                                        "${offset + i + 1}. $word",
                                        fontFamily = FontFamily.Monospace,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSeedPhrase = false }) { Text("Done") }
            },
        )
    }
}
