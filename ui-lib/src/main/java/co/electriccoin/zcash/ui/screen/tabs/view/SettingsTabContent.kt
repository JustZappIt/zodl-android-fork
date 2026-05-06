package co.electriccoin.zcash.ui.screen.tabs.view

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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.electriccoin.zcash.ui.NavigationRouter
import co.electriccoin.zcash.ui.common.viewmodel.SecretState
import co.electriccoin.zcash.ui.common.viewmodel.WalletViewModel
import co.electriccoin.zcash.ui.design.component.zapp.ZappButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappButtonVariant
import co.electriccoin.zcash.ui.design.component.zapp.ZappGroupHeader
import co.electriccoin.zcash.ui.design.component.zapp.ZappRow
import co.electriccoin.zcash.ui.design.component.zapp.ZappRowDivider
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.component.zapp.ellipsizeAddress
import co.electriccoin.zcash.ui.design.component.zapp.initialsOf
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.design.theme.colors.ZappNavBar
import co.electriccoin.zcash.ui.screen.about.AboutArgs
import co.electriccoin.zcash.ui.screen.chat.ChatProfileArgs
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel
import co.electriccoin.zcash.ui.screen.chooseserver.ChooseServerArgs
import co.electriccoin.zcash.ui.screen.swap.SwapArgs
import co.electriccoin.zcash.ui.screen.taxexport.TaxExport
import co.electriccoin.zcash.ui.screen.tor.optin.TorOptInArgs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTabContent(
    navigationRouter: NavigationRouter,
    chatViewModel: ChatViewModel,
    walletViewModel: WalletViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val c = ZappTheme.colors
    val identity by chatViewModel.identity.collectAsState()
    val secretState by walletViewModel.secretState.collectAsStateWithLifecycle()
    val hasWallet = secretState == SecretState.READY
    val snackbarHostState = remember { SnackbarHostState() }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    var showCopiedFeedback by remember { mutableStateOf(false) }
    var showDeleteIdentityConfirm by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = c.bg,
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            ZappScreenHeader(title = "Settings")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = ZappNavBar.CLEARANCE_DP.dp),
            ) {
                identity?.let { id ->
                    ProfileCard(
                        displayName = id.displayName,
                        publicKey = id.publicKey,
                        showCopiedFeedback = showCopiedFeedback,
                        onEditName = {
                            editNameText = id.displayName
                            showEditNameDialog = true
                        },
                        onCopyKey = {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(
                                ClipData.newPlainText("Public Key", id.publicKey),
                            )
                            showCopiedFeedback = true
                            scope.launch {
                                delay(2000)
                                showCopiedFeedback = false
                            }
                        },
                    )
                }

                SettingsGroup(title = "Security") {
                    ZappRow(
                        title = "Profile & identity",
                        subtitle = "Seed phrase, backup, display name",
                        icon = Icons.Default.Key,
                        iconTint = c.accentText,
                        iconBackground = c.accentSoft,
                        onClick = { navigationRouter.forward(ChatProfileArgs) },
                    )
                    // HIDDEN: Backup / restore — uncomment to restore (and the divider above)
                    // ZappRowDivider(inset = true)
                    // ZappRow(
                    //     title = "Backup / restore",
                    //     subtitle = "Coming soon",
                    //     icon = Icons.Default.Backup,
                    //     onClick = {
                    //         scope.launch { snackbarHostState.showSnackbar("Backup & restore coming soon.") }
                    //     },
                    // )
                }

                if (hasWallet) {
                    SettingsGroup(title = "Wallet") {
                        // HIDDEN: Backup seed phrase — uncomment to restore (and the divider below)
                        // ZappRow(
                        //     title = "Backup seed phrase",
                        //     subtitle = "View and save your 24-word recovery phrase",
                        //     icon = Icons.Default.AccountBalanceWallet,
                        //     iconTint = c.accentText,
                        //     iconBackground = c.accentSoft,
                        //     onClick = { navigationRouter.forward(AdvancedSettingsArgs) },
                        // )
                        // ZappRowDivider(inset = true)
                        ZappRow(
                            title = "Server",
                            subtitle = "Choose a lightwalletd server",
                            icon = Icons.Default.Cloud,
                            onClick = { navigationRouter.forward(ChooseServerArgs) },
                        )
                    }
                }

                SettingsGroup(title = "Support") {
                    ZappRow(
                        title = "Contact support",
                        subtitle = "Report issues, share feedback",
                        icon = Icons.Default.SupportAgent,
                        onClick = {
                            scope.launch { snackbarHostState.showSnackbar("Support chat coming soon.") }
                        },
                    )
                }

                // HIDDEN: About — uncomment to restore
                // SettingsGroup(title = "About") {
                //     ZappRow(
                //         title = "About Zapp",
                //         icon = Icons.Default.Info,
                //         onClick = { navigationRouter.forward(AboutArgs) },
                //     )
                // }

                SettingsGroup(title = "Advanced") {
                    ZappRow(
                        title = "Swap",
                        subtitle = "Exchange ZEC for other assets via Flexa",
                        icon = Icons.Default.CurrencyExchange,
                        onClick = { navigationRouter.forward(SwapArgs) },
                    )
                    // HIDDEN: Tor / Privacy — uncomment to restore (and the divider above)
                    // ZappRowDivider(inset = true)
                    // ZappRow(
                    //     title = "Tor / Privacy",
                    //     subtitle = "Route traffic through the Tor network",
                    //     icon = Icons.Default.VpnLock,
                    //     onClick = { navigationRouter.forward(TorOptInArgs) },
                    // )
                    // HIDDEN: Tax export — uncomment to restore (and the divider above)
                    // ZappRowDivider(inset = true)
                    // ZappRow(
                    //     title = "Tax export",
                    //     subtitle = "Export transaction history for tax reporting",
                    //     icon = Icons.Default.Receipt,
                    //     onClick = { navigationRouter.forward(TaxExport) },
                    // )
                }

                Spacer(Modifier.height(20.dp))

                Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                    ZappButton(
                        text = "Delete identity",
                        variant = ZappButtonVariant.Danger,
                        onClick = { showDeleteIdentityConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    BasicText(
                        text = "Permanently removes this identity and its chat history from this device.",
                        style = ZappTheme.typography.caption.copy(color = c.textSubtle),
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

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
                            chatViewModel.updateDisplayName(editNameText.trim())
                            showEditNameDialog = false
                        }
                        .heightIn(min = 48.dp)
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
                        .heightIn(min = 48.dp)
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

    if (showDeleteIdentityConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteIdentityConfirm = false },
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
                    text = "This will remove your identity, contacts, and messages from this device. " +
                        "You'll need your seed phrase to restore them.",
                    style = ZappTheme.typography.body.copy(color = c.textMuted),
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clickable {
                            showDeleteIdentityConfirm = false
                            chatViewModel.deleteIdentity {
                                scope.launch { snackbarHostState.showSnackbar("Identity deleted.") }
                            }
                        }
                        .heightIn(min = 48.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Delete",
                        style = ZappTheme.typography.button.copy(color = c.danger),
                    )
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clickable { showDeleteIdentityConfirm = false }
                        .heightIn(min = 48.dp)
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

}

@Composable
private fun ProfileCard(
    displayName: String,
    publicKey: String,
    showCopiedFeedback: Boolean,
    onEditName: () -> Unit,
    onCopyKey: () -> Unit,
) {
    val c = ZappTheme.colors
    val initials = remember(displayName) { initialsOf(displayName) }
    val shortKey = remember(publicKey) { publicKey.ellipsizeAddress() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
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
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            BasicText(
                text = "@$displayName",
                style = ZappTheme.typography.sectionTitle.copy(color = c.text),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(
                onClick = onEditName,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit display name",
                    tint = c.textMuted,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .background(c.surfaceAlt, RectangleShape)
                .clickable(onClick = onCopyKey)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = shortKey,
                style = ZappTheme.typography.mono.copy(color = c.textMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = if (showCopiedFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                contentDescription = "Copy public key",
                tint = if (showCopiedFeedback) c.success else c.accent,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    val c = ZappTheme.colors
    ZappGroupHeader(text = title)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .background(c.surface, RectangleShape)
            .border(BorderStroke(1.dp, c.border), RectangleShape),
    ) {
        content()
    }
    Spacer(Modifier.height(8.dp))
}
