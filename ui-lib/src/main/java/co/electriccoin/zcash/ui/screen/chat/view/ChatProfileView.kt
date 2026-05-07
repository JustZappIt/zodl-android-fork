package co.electriccoin.zcash.ui.screen.chat.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.electriccoin.zcash.ui.common.model.WalletAccount
import co.electriccoin.zcash.ui.design.component.QrState
import co.electriccoin.zcash.ui.design.component.ZashiQr
import co.electriccoin.zcash.ui.design.component.zapp.ZappRow
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.component.zapp.initialsOf
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel
import co.electriccoin.zcash.ui.screen.onboarding.view.PinVerifyScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class ProfileTab { MessagingId, WalletAddress }
private enum class WalletSubTab { Shielded, Transparent }

@Composable
fun ChatProfileView(
    onNavigateBack: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onIdentityDeleted: () -> Unit,
    walletAccount: WalletAccount?,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
) {
    val c = ZappTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val identity by viewModel.identity.collectAsState()
    val pinVerifyState by viewModel.pinVerifyState.collectAsState()
    val pendingSeedPhrase by viewModel.pendingSeedPhrase.collectAsState()

    var activeTab by remember { mutableStateOf(ProfileTab.MessagingId) }
    var walletSubTab by remember { mutableStateOf(WalletSubTab.Shielded) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    var showKeyCopied by remember { mutableStateOf(false) }
    var showAddrCopied by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        ZappScreenHeader(title = "Profile & Identity")

        // ── Scrollable body ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (activeTab) {
                ProfileTab.MessagingId -> {
                    val initials = remember(identity?.displayName) {
                        identity?.displayName?.let { initialsOf(it) } ?: "?"
                    }

                    // Avatar + username
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(c.accent, RectangleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = initials,
                                style = ZappTheme.typography.sectionTitle.copy(color = c.onAccent),
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            BasicText(
                                text = "@${identity?.displayName ?: ""}",
                                style = ZappTheme.typography.sectionTitle.copy(color = c.text),
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = false),
                                    ) {
                                        editNameText = identity?.displayName ?: ""
                                        showEditNameDialog = true
                                    }
                                    .semantics { contentDescription = "Edit display name"; role = Role.Button },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = c.textMuted,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }

                    // QR card
                    identity?.publicKey?.let { pk ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(c.surface, RectangleShape)
                                .border(BorderStroke(1.dp, c.border), RectangleShape)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ZashiQr(state = QrState(qrData = pk), qrSize = 160.dp)
                                BasicText(
                                    text = "Scan to start a conversation",
                                    style = ZappTheme.typography.caption.copy(color = c.textSubtle),
                                )
                            }
                        }

                        // Public Key card
                        PublicKeyCard(
                            publicKey = pk,
                            showCopiedFeedback = showKeyCopied,
                            onCopy = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Public Key", pk))
                                showKeyCopied = true
                                scope.launch { delay(2000); showKeyCopied = false }
                            },
                        )
                    }
                }

                ProfileTab.WalletAddress -> {
                    val address = when (walletSubTab) {
                        WalletSubTab.Shielded ->
                            walletAccount?.unified?.address?.address ?: ""
                        WalletSubTab.Transparent ->
                            walletAccount?.transparent?.address?.address ?: ""
                    }
                    val addressLabel = when (walletSubTab) {
                        WalletSubTab.Shielded -> "Shielded Address"
                        WalletSubTab.Transparent -> "Transparent Address"
                    }
                    val caption = when (walletSubTab) {
                        WalletSubTab.Shielded -> "Shielded address — private by default"
                        WalletSubTab.Transparent -> "Transparent address — visible on chain"
                    }

                    // QR card
                    if (address.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(c.surface, RectangleShape)
                                .border(BorderStroke(1.dp, c.border), RectangleShape)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ZashiQr(state = QrState(qrData = address), qrSize = 200.dp)
                                BasicText(
                                    text = caption,
                                    style = ZappTheme.typography.caption.copy(color = c.textSubtle),
                                )
                            }
                        }

                        // Address card
                        AddressCard(
                            label = addressLabel,
                            address = address,
                            showCopiedFeedback = showAddrCopied,
                            onCopy = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText(addressLabel, address))
                                showAddrCopied = true
                                scope.launch { delay(2000); showAddrCopied = false }
                            },
                        )
                    }
                }
            }
        }

        // ── Fixed bottom panel ───────────────────────────────────────────

        // Seed Phrase row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .background(c.surface, RectangleShape)
                .border(BorderStroke(1.dp, c.border), RectangleShape),
        ) {
            ZappRow(
                title = "Seed Phrase",
                subtitle = "Backs up both messaging & wallet identities",
                icon = Icons.Default.Key,
                iconBackground = c.accentSoft,
                iconTint = c.accentText,
                onClick = { viewModel.onSeedPhraseRequested() },
            )
        }

        Spacer(Modifier.height(4.dp))

        // Wallet sub-tabs (Shielded / Transparent) — only on Wallet Address tab
        if (activeTab == ProfileTab.WalletAddress && walletAccount != null) {
            ProfileSegmentedRow(
                items = listOf(
                    SegmentItem("Shielded", Icons.Default.Security, walletSubTab == WalletSubTab.Shielded),
                    SegmentItem("Transparent", Icons.Default.CreditCard, walletSubTab == WalletSubTab.Transparent),
                ),
                onSelect = { idx ->
                    walletSubTab = if (idx == 0) WalletSubTab.Shielded else WalletSubTab.Transparent
                },
            )
            Spacer(Modifier.height(4.dp))
        }

        // Main tab switcher (Messaging ID / Wallet Address)
        ProfileSegmentedRow(
            items = listOf(
                SegmentItem("Messaging ID", null, activeTab == ProfileTab.MessagingId),
                SegmentItem("Wallet Address", null, activeTab == ProfileTab.WalletAddress),
            ),
            onSelect = { idx ->
                activeTab = if (idx == 0) ProfileTab.MessagingId else ProfileTab.WalletAddress
            },
        )

        Spacer(Modifier.height(12.dp))

        // Bottom dock: ← | DELETE IDENTITY
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
                    text = "←",
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
                    .background(c.danger, RectangleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = c.onAccent),
                        onClick = { showDeleteDialog = true },
                    )
                    .semantics { contentDescription = "Delete Identity"; role = Role.Button },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "DELETE IDENTITY",
                    style = ZappTheme.typography.button.copy(
                        color = c.onAccent,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                    ),
                )
            }
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = c.surface,
            titleContentColor = c.text,
            textContentColor = c.textMuted,
            shape = RectangleShape,
            title = {
                BasicText(
                    text = "Edit display name",
                    style = ZappTheme.typography.sectionTitle.copy(color = c.text),
                )
            },
            text = {
                OutlinedTextField(
                    value = editNameText,
                    onValueChange = { editNameText = it },
                    singleLine = true,
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clickable(enabled = editNameText.isNotBlank()) {
                            viewModel.updateDisplayName(editNameText.trim())
                            showEditNameDialog = false
                        }
                        .defaultMinSize(minHeight = 48.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Save",
                        style = ZappTheme.typography.button.copy(
                            color = if (editNameText.isNotBlank()) c.accent else c.textSubtle,
                        ),
                    )
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clickable { showEditNameDialog = false }
                        .defaultMinSize(minHeight = 48.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Cancel",
                        style = ZappTheme.typography.button.copy(color = c.textMuted),
                    )
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = c.surface,
            titleContentColor = c.text,
            textContentColor = c.textMuted,
            shape = RectangleShape,
            title = {
                BasicText(
                    text = "Delete identity?",
                    style = ZappTheme.typography.sectionTitle.copy(color = c.text),
                )
            },
            text = {
                BasicText(
                    text = "This will remove your messaging identity and all chat history from this device. " +
                        "Your wallet funds are unaffected and can be recovered at any time with your seed phrase.",
                    style = ZappTheme.typography.body.copy(color = c.textMuted),
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clickable {
                            showDeleteDialog = false
                            viewModel.deleteIdentity { onIdentityDeleted() }
                        }
                        .defaultMinSize(minHeight = 48.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Delete",
                        style = ZappTheme.typography.rowTitle.copy(color = c.danger),
                    )
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clickable { showDeleteDialog = false }
                        .defaultMinSize(minHeight = 48.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Cancel",
                        style = ZappTheme.typography.rowTitle.copy(color = c.textMuted),
                    )
                }
            },
        )
    }

    // PIN overlay — blocks until verified or dismissed
    if (pinVerifyState != ChatViewModel.PinVerifyState.Idle) {
        PinVerifyScreen(
            hasError = pinVerifyState == ChatViewModel.PinVerifyState.Error,
            lockoutSecondsRemaining =
                (pinVerifyState as? ChatViewModel.PinVerifyState.Locked)?.secondsRemaining ?: 0,
            onPinSubmit = { viewModel.onPinSubmitted(it) },
            onCancel = { viewModel.onPinEntryDismissed() },
        )
    }

    pendingSeedPhrase?.let { phrase ->
        SeedPhraseDialog(
            seedPhrase = phrase,
            onDismiss = { viewModel.consumeSeedPhrase() },
        )
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun PublicKeyCard(
    publicKey: String,
    showCopiedFeedback: Boolean,
    onCopy: () -> Unit,
) {
    val c = ZappTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceAlt, RectangleShape)
            .border(BorderStroke(1.dp, c.border), RectangleShape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = "Public Key",
                style = ZappTheme.typography.caption.copy(color = c.textMuted),
            )
            Spacer(Modifier.height(4.dp))
            BasicText(
                text = publicKey,
                style = ZappTheme.typography.mono.copy(color = c.text),
                maxLines = 3,
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onCopy)
                .semantics { contentDescription = if (showCopiedFeedback) "Copied" else "Copy public key"; role = Role.Button },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (showCopiedFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                contentDescription = null,
                tint = if (showCopiedFeedback) c.success else c.textMuted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun AddressCard(
    label: String,
    address: String,
    showCopiedFeedback: Boolean,
    onCopy: () -> Unit,
) {
    val c = ZappTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceAlt, RectangleShape)
            .border(BorderStroke(1.dp, c.border), RectangleShape)
            .padding(16.dp),
    ) {
        BasicText(
            text = label,
            style = ZappTheme.typography.caption.copy(color = c.textMuted),
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                text = address,
                style = ZappTheme.typography.mono.copy(color = c.text),
                modifier = Modifier.weight(1f),
                maxLines = 3,
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onCopy)
                    .semantics {
                        contentDescription = if (showCopiedFeedback) "Copied" else "Copy address"
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (showCopiedFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = if (showCopiedFeedback) c.success else c.textMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private data class SegmentItem(val label: String, val icon: ImageVector?, val isSelected: Boolean)

@Composable
private fun ProfileSegmentedRow(
    items: List<SegmentItem>,
    onSelect: (Int) -> Unit,
) {
    val c = ZappTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .background(c.surfaceAlt, RectangleShape)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items.forEachIndexed { index, item ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 40.dp)
                    .background(if (item.isSelected) c.surface else Color.Transparent, RectangleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = c.accent),
                    ) { onSelect(index) }
                    .semantics { contentDescription = item.label; role = Role.Button },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (item.icon != null) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (item.isSelected) c.accentText else c.textMuted,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    BasicText(
                        text = item.label,
                        style = ZappTheme.typography.rowSubtitle.copy(
                            color = if (item.isSelected) c.text else c.textMuted,
                            fontWeight = if (item.isSelected) FontWeight.Black else FontWeight.Normal,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun SeedPhraseDialog(
    seedPhrase: String,
    onDismiss: () -> Unit,
) {
    val c = ZappTheme.colors
    val words = remember(seedPhrase) { seedPhrase.split(" ").filter { it.isNotBlank() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        titleContentColor = c.text,
        textContentColor = c.textMuted,
        shape = RectangleShape,
        title = {
            BasicText(
                text = "Seed Phrase",
                style = ZappTheme.typography.sectionTitle.copy(color = c.text),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BasicText(
                    text = "Keep this safe. Anyone with these words can access your wallet and messages.",
                    style = ZappTheme.typography.body.copy(color = c.textMuted),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    listOf(words.take(12) to 0, words.drop(12) to 12).forEach { (col, offset) ->
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            col.forEachIndexed { i, word ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    BasicText(
                                        text = "${offset + i + 1}".padStart(2, '0'),
                                        style = ZappTheme.typography.mono.copy(color = c.textSubtle),
                                    )
                                    BasicText(
                                        text = word,
                                        style = ZappTheme.typography.rowTitle.copy(color = c.text),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clickable(onClick = onDismiss)
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Done",
                    style = ZappTheme.typography.rowTitle.copy(color = c.accent),
                )
            }
        },
    )
}
