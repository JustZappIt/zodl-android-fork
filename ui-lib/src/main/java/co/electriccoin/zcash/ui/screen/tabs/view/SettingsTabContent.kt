package co.electriccoin.zcash.ui.screen.tabs.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.electriccoin.zcash.ui.NavigationRouter
import co.electriccoin.zcash.ui.common.viewmodel.SecretState
import co.electriccoin.zcash.ui.common.viewmodel.WalletViewModel
import co.electriccoin.zcash.ui.design.theme.colors.ZappPalette
import co.electriccoin.zcash.ui.screen.about.AboutArgs
import co.electriccoin.zcash.ui.screen.advancedsettings.AdvancedSettingsArgs
import co.electriccoin.zcash.ui.screen.chat.ChatProfileArgs
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel
import co.electriccoin.zcash.ui.screen.chooseserver.ChooseServerArgs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Port of Zapp's SettingsScreen layout — a stack of rounded "settings card"
 * sections. Identity card + security rows + wallet stub + about + danger zone.
 *
 * Sub-screens that don't have a zodl equivalent yet (wallet link sheet,
 * seed phrase view, backup/restore, language picker, hand preference) link
 * to existing zodl routes where possible or show a "coming soon" snackbar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTabContent(
    navigationRouter: NavigationRouter,
    chatViewModel: ChatViewModel,
    walletViewModel: WalletViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val identity by chatViewModel.identity.collectAsState()
    val secretState by walletViewModel.secretState.collectAsStateWithLifecycle()
    val hasWallet = secretState == SecretState.READY
    val snackbarHostState = remember { SnackbarHostState() }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    var showCopiedFeedback by remember { mutableStateOf(false) }
    var showDeleteIdentityConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            identity?.let { id ->
                SettingsCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(ZappPalette.Primary, ZappPalette.Accent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = id.displayName.take(2).uppercase(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ZappPalette.OnPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                id.displayName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = {
                                    editNameText = id.displayName
                                    showEditNameDialog = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit name",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val clipboard =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(
                                        ClipData.newPlainText("Public Key", id.publicKey)
                                    )
                                    showCopiedFeedback = true
                                    scope.launch {
                                        delay(2000)
                                        showCopiedFeedback = false
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "${id.publicKey.take(10)}...${id.publicKey.takeLast(6)}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (showCopiedFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy public key",
                                tint = if (showCopiedFeedback) ZappPalette.Success else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            SettingsCard {
                SettingsSectionTitle("Security")
                SettingsRow(
                    title = "Profile & identity",
                    subtitle = "Seed phrase, backup, display name",
                    icon = Icons.Default.Key,
                    onClick = { navigationRouter.forward(ChatProfileArgs) }
                )
                SettingsDivider()
                SettingsRow(
                    title = "Backup / restore",
                    subtitle = "Coming soon",
                    icon = Icons.Default.Backup,
                    onClick = {
                        scope.launch { snackbarHostState.showSnackbar("Backup & restore coming soon.") }
                    }
                )
            }

            if (hasWallet) {
                SettingsCard {
                    SettingsSectionTitle("Wallet")
                    SettingsRow(
                        title = "Backup seed phrase",
                        subtitle = "View and save your 24-word recovery phrase",
                        icon = Icons.Default.AccountBalanceWallet,
                        onClick = { navigationRouter.forward(AdvancedSettingsArgs) }
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Server",
                        subtitle = "Choose a lightwalletd server",
                        icon = Icons.Default.Cloud,
                        onClick = { navigationRouter.forward(ChooseServerArgs) }
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Advanced wallet settings",
                        subtitle = "Export, privacy, resync, and more",
                        icon = Icons.Default.Tune,
                        onClick = { navigationRouter.forward(AdvancedSettingsArgs) }
                    )
                }
            }

            SettingsCard {
                SettingsSectionTitle("Support")
                SettingsRow(
                    title = "Contact support",
                    subtitle = "Report issues, share feedback",
                    icon = Icons.Default.SupportAgent,
                    onClick = {
                        scope.launch { snackbarHostState.showSnackbar("Support chat coming soon.") }
                    }
                )
            }

            SettingsCard {
                SettingsSectionTitle("About")
                SettingsRow(
                    title = "About Zapp",
                    icon = Icons.Default.Info,
                    onClick = { navigationRouter.forward(AboutArgs) }
                )
            }

            SettingsCard {
                SettingsSectionTitle("Danger zone")
                SettingsRow(
                    title = "Delete identity",
                    subtitle = "Permanently remove this identity and its chat history",
                    onClick = { showDeleteIdentityConfirm = true },
                    titleColor = ZappPalette.Error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit display name") },
            text = {
                OutlinedTextField(
                    value = editNameText,
                    onValueChange = { editNameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = editNameText.isNotBlank(),
                    onClick = {
                        chatViewModel.updateDisplayName(editNameText.trim())
                        showEditNameDialog = false
                    }
                ) { Text("Save", color = ZappPalette.Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteIdentityConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteIdentityConfirm = false },
            title = { Text("Delete identity?") },
            text = {
                Text(
                    "This will remove your identity, contacts, and messages from this device. " +
                        "You'll need your seed phrase to restore them."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteIdentityConfirm = false
                    chatViewModel.deleteIdentity {
                        scope.launch {
                            snackbarHostState.showSnackbar("Identity deleted.")
                        }
                    }
                }) { Text("Delete", color = ZappPalette.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteIdentityConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
    ) {
        content()
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    titleColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = if (titleColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else titleColor
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}
