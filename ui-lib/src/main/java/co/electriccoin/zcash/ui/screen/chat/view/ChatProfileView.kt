package co.electriccoin.zcash.ui.screen.chat.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import co.electriccoin.zcash.ui.design.component.QrState
import co.electriccoin.zcash.ui.design.component.ZashiQr
import co.electriccoin.zcash.ui.design.component.zapp.ZappBackButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappButtonVariant
import co.electriccoin.zcash.ui.design.component.zapp.ZappRow
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.component.zapp.initialsOf
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel
import co.electriccoin.zcash.ui.screen.onboarding.view.PinVerifyScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatProfileView(
    onNavigateBack: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onIdentityDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
) {
    val c = ZappTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val identity by viewModel.identity.collectAsState()
    val pinVerifyState by viewModel.pinVerifyState.collectAsState()
    val pendingSeedPhrase by viewModel.pendingSeedPhrase.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCopiedFeedback by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ZappScreenHeader(
                title = "Profile",
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.surface)
                    .border(BorderStroke(1.dp, c.border), RectangleShape)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ZappBackButton(onClick = onNavigateBack)
                ZappButton(
                    text = "Delete Identity",
                    variant = ZappButtonVariant.Danger,
                    leadingIcon = Icons.Default.Delete,
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                    onClick = { showDeleteDialog = true },
                )
            }
        },
        containerColor = c.bg,
        contentWindowInsets = WindowInsets(0),
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
            val initials = remember(identity?.displayName) {
                identity?.displayName?.let { initialsOf(it) } ?: "?"
            }

            // Avatar
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
                text = "@${identity?.displayName ?: "Unknown"}",
                style = ZappTheme.typography.sectionTitle.copy(color = c.text),
            )

            identity?.publicKey?.let { pk ->
                ZashiQr(
                    state = QrState(qrData = pk),
                    qrSize = 200.dp,
                )

                PublicKeyCard(
                    publicKey = pk,
                    showCopiedFeedback = showCopiedFeedback,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Public Key", pk))
                        showCopiedFeedback = true
                        scope.launch { delay(2000); showCopiedFeedback = false }
                    },
                )
            }

            ZappRow(
                title = "Seed Phrase",
                subtitle = "Recovery words for wallet & messaging",
                icon = Icons.Default.Key,
                iconBackground = c.accentSoft,
                iconTint = c.accentText,
                onClick = { viewModel.onSeedPhraseRequested() },
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = c.surface,
            titleContentColor = c.text,
            textContentColor = c.textMuted,
            shape = RectangleShape,
            title = { BasicText("Delete identity?", style = ZappTheme.typography.sectionTitle.copy(color = c.text)) },
            text = {
                BasicText(
                    text = "This will remove your messaging identity and all chat history from this device. " +
                        "You'll need your seed phrase to restore it.",
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
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    BasicText("Delete", style = ZappTheme.typography.rowTitle.copy(color = c.danger))
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clickable { showDeleteDialog = false }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    BasicText("Cancel", style = ZappTheme.typography.rowTitle.copy(color = c.textMuted))
                }
            },
        )
    }

    // PIN overlay — takes over the full screen until dismissed or verified.
    // Picks up the global PinAuthGate lockout via the Locked sub-state so the
    // keypad disables and shows a countdown if the user has burned through
    // attempts on any other PIN surface in the app.
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
                .clickable(onClick = onCopy),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (showCopiedFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                contentDescription = if (showCopiedFeedback) "Copied" else "Copy public key",
                tint = if (showCopiedFeedback) c.success else c.textMuted,
                modifier = Modifier.size(20.dp),
            )
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
        title = { BasicText("Seed Phrase", style = ZappTheme.typography.sectionTitle.copy(color = c.text)) },
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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                BasicText("Done", style = ZappTheme.typography.rowTitle.copy(color = c.accent))
            }
        },
    )
}
