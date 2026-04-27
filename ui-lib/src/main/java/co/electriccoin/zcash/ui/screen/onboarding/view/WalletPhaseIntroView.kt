@file:Suppress("TooManyFunctions")

package co.electriccoin.zcash.ui.screen.onboarding.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.electriccoin.zcash.ui.design.theme.ZappTheme

// ───────────────────────────────────────────────────────────────
// 05 · Phase 2 intro — Wallet
// ───────────────────────────────────────────────────────────────

@Composable
fun WalletPhaseIntro(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit = {},
) {
    OnbScreen(
        step = 4,
        ghostNum = 5,
        badge = "Part 2 of 3 · Wallet",
        cta = "Continue",
        onCta = onContinue,
        showBack = true,
        onBack = onBack,
    ) {
        OnbHero(text = "Now set up\nyour wallet")
        Spacer(Modifier.height(16.dp))
        OnbSub(
            text = "Your wallet is separate from your messaging identity. It has its own recovery phrase. You can also skip this and add a wallet later.",
            modifier = Modifier.fillMaxWidth(0.94f),
        )
        Spacer(Modifier.height(28.dp))
        OnbBulletRow(
            label = "Create a new wallet",
            sub = "Or restore one you already have",
            isFirst = true,
        )
        OnbBulletRow(
            label = "Save a second recovery phrase",
            sub = "Different words — keep both safe",
        )
        Spacer(Modifier.height(20.dp))
        SkipLink(onSkip)
    }
}

// ───────────────────────────────────────────────────────────────
// 06 · Wallet choice — Create / Restore (skip via dock)
// ───────────────────────────────────────────────────────────────

@Composable
fun WalletChoiceScreen(
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onRestore: () -> Unit,
    onSkip: () -> Unit = {},
) {
    OnbScreen(
        step = 4,
        ghostNum = 6,
        badge = "Part 2 · Wallet",
        cta = "Skip wallet for now",
        onCta = onSkip,
        showBack = true,
        onBack = onBack,
    ) {
        OnbHero(text = "Set up\nyour wallet")
        Spacer(Modifier.height(14.dp))
        OnbSub("Your wallet lives on this device. Only you hold the keys.")
        Spacer(Modifier.height(28.dp))

        OnbActionListCard(
            actions = listOf(
                OnbAction(
                    icon = "✦",
                    label = "Create new wallet",
                    sub = "Get a fresh recovery phrase",
                    onClick = onCreate,
                    highlight = true,
                ),
                OnbAction(
                    icon = "⚿",
                    label = "Restore from phrase",
                    sub = "Use your existing recovery phrase",
                    onClick = onRestore,
                ),
            ),
        )
    }
}

// ───────────────────────────────────────────────────────────────
// 07 · Wallet recovery phrase
// ───────────────────────────────────────────────────────────────

@Composable
fun WalletSeedPhraseScreen(
    words: List<String>,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    SeedRevealScreen(
        step = 5,
        title = "Wallet\nrecovery phrase",
        sub = "These words restore your funds. Different from your messaging phrase.",
        words = words,
        onBack = onBack,
        onContinue = onContinue,
    )
}

@Composable
private fun SkipLink(onSkip: () -> Unit) {
    val c = ZappTheme.colors
    Row(
        modifier = Modifier
            .clickable(onClick = onSkip)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = "Skip wallet — I'll add it later",
            style = ZappTheme.typography.button.copy(
                color = c.accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.2.sp,
            ),
        )
    }
}
