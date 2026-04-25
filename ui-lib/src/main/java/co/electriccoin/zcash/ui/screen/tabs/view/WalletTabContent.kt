package co.electriccoin.zcash.ui.screen.tabs.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.electriccoin.zcash.ui.NavigationRouter
import co.electriccoin.zcash.ui.common.viewmodel.SecretState
import co.electriccoin.zcash.ui.common.viewmodel.WalletViewModel
import co.electriccoin.zcash.ui.design.component.zapp.ZappButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappButtonVariant
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.component.zapp.ZappSectionLabel
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.design.theme.colors.ZappNavBar
import co.electriccoin.zcash.ui.screen.restore.seed.RestoreSeedArgs
import org.koin.androidx.compose.koinViewModel

@Composable
fun WalletTabContent(
    navigationRouter: NavigationRouter,
    walletViewModel: WalletViewModel = koinViewModel(),
) {
    val c = ZappTheme.colors
    val secretState by walletViewModel.secretState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        when (secretState) {
            SecretState.LOADING ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = c.accent)
                }
            SecretState.NONE ->
                WalletSetupView(
                    onCreateWallet = { walletViewModel.createNewWallet() },
                    onImportWallet = { navigationRouter.forward(RestoreSeedArgs) },
                )
            SecretState.READY -> WalletHomeView()
        }
    }
}

@Composable
private fun WalletSetupView(
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit,
) {
    val c = ZappTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        ZappScreenHeader(
            title = "Wallet",
            subtitle = "Set up your Zcash wallet",
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = ZappNavBar.CLEARANCE_DP.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(c.accent, RectangleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = c.onAccent,
                    modifier = Modifier.size(44.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BasicText(
                    text = "Your Zcash wallet",
                    style = ZappTheme.typography.sectionTitle.copy(color = c.text),
                )
                BasicText(
                    text = "Create a new wallet to get started, or restore one you already have using your seed phrase.",
                    style = ZappTheme.typography.body.copy(
                        color = c.textMuted,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
            }

            Spacer(Modifier.height(4.dp))

            ZappSectionLabel(
                text = "What to expect",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 2.dp, bottom = 2.dp),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                WalletInfoTile(
                    icon = Icons.Filled.Shield,
                    title = "Stays on your device",
                    subtitle = "Your seed phrase is generated and stored locally. Only you control it.",
                )
                WalletInfoTile(
                    icon = Icons.Default.Download,
                    title = "Restore from seed",
                    subtitle = "Have an existing Zcash wallet? Enter your 24-word seed phrase to restore it.",
                )
            }

            Spacer(Modifier.height(4.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ZappButton(
                    text = "Create new wallet",
                    onClick = onCreateWallet,
                    modifier = Modifier.fillMaxWidth(),
                )
                ZappButton(
                    text = "Restore existing wallet",
                    variant = ZappButtonVariant.Ghost,
                    onClick = onImportWallet,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            BasicText(
                text = "You can keep using chats and settings without a wallet.",
                style = ZappTheme.typography.caption.copy(
                    color = c.textSubtle,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun WalletInfoTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
) {
    val c = ZappTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface, RectangleShape)
            .border(BorderStroke(1.dp, c.border), RectangleShape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(c.accentSoft, RectangleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = c.accentText,
                modifier = Modifier.size(22.dp),
            )
        }
        Column {
            BasicText(
                text = title,
                style = ZappTheme.typography.rowTitle.copy(color = c.text),
            )
            Spacer(Modifier.height(2.dp))
            BasicText(
                text = subtitle,
                style = ZappTheme.typography.rowSubtitle.copy(color = c.textMuted),
            )
        }
    }
}
