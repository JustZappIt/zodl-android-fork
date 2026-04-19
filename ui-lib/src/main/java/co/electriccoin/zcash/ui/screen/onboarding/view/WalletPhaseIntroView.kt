package co.electriccoin.zcash.ui.screen.onboarding.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.electriccoin.zcash.ui.design.component.ZashiButton
import co.electriccoin.zcash.ui.design.component.ZashiTopAppBarBackNavigation
import co.electriccoin.zcash.ui.design.newcomponent.PreviewScreens
import co.electriccoin.zcash.ui.design.theme.ZcashTheme
import co.electriccoin.zcash.ui.design.theme.colors.ZappPalette
import co.electriccoin.zcash.ui.design.theme.colors.ZashiColors
import co.electriccoin.zcash.ui.design.theme.typography.ZashiTypography

// ───────────────────────────────────────────────
// Step 4/6 — Wallet phase intro
// ───────────────────────────────────────────────

@Composable
fun WalletPhaseIntro(
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    Scaffold { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(16.dp))

            OnboardingProgressBar(step = 4, total = 6)

            Spacer(Modifier.height(24.dp))

            ZashiTopAppBarBackNavigation(onBack = onBack)

            Spacer(Modifier.height(24.dp))

            // Phase badge
            Text(
                text = "PART 2 OF 3 · WALLET",
                color = ZappPalette.Primary,
                style = ZashiTypography.textXs,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Set up your wallet",
                style = ZashiTypography.header6,
                fontWeight = FontWeight.Bold,
                color = ZashiColors.Text.textPrimary,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Your wallet is independent from your messaging identity. It holds your ZEC and requires its own recovery phrase.",
                style = ZashiTypography.textSm,
                color = ZashiColors.Text.textTertiary,
            )

            Spacer(Modifier.height(24.dp))

            BulletRowWallet(
                icon = "⊕",
                title = "Create or restore",
                detail = "Generate a new wallet or restore an existing one from a 24-word seed phrase.",
            )

            Spacer(Modifier.height(16.dp))

            BulletRowWallet(
                icon = "⊕",
                title = "Second recovery phrase",
                detail = "Your wallet uses a separate 24-word BIP-39 phrase — keep it stored securely offline.",
            )

            Spacer(Modifier.weight(1f))

            ZashiButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Continue",
                onClick = onContinue,
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BulletRowWallet(
    icon: String,
    title: String,
    detail: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(ZashiColors.Surfaces.bgSecondary)
                .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = icon,
            color = ZappPalette.Primary,
            style = ZashiTypography.textSm,
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = ZashiTypography.textSm,
                fontWeight = FontWeight.SemiBold,
                color = ZashiColors.Text.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = detail,
                style = ZashiTypography.textXs,
                color = ZashiColors.Text.textTertiary,
            )
        }
    }
}

@PreviewScreens
@Composable
private fun WalletPhaseIntroPreview() =
    ZcashTheme {
        WalletPhaseIntro(onBack = {}, onContinue = {})
    }
