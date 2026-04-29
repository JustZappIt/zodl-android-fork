@file:Suppress("TooManyFunctions")

package co.electriccoin.zcash.ui.screen.onboarding.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ───────────────────────────────────────────────────────────────
// 05 · Phase 2 intro — Wallet
// ───────────────────────────────────────────────────────────────

@Composable
fun WalletPhaseIntro(
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    OnbScreen(
        step = 3,
        ghostNum = 4,
        badge = "Part 2 of 3 · Wallet",
        cta = "Continue",
        onCta = onContinue,
        showBack = true,
        onBack = onBack,
    ) {
        OnbHero(text = "Now set up\nyour wallet")
        Spacer(Modifier.height(16.dp))
        OnbSub(
            text = "Your wallet holds your ZEC. Its 24-word recovery phrase also backs up your messaging identity — one phrase for everything.",
            modifier = Modifier.fillMaxWidth(0.94f),
        )
        Spacer(Modifier.height(28.dp))
        OnbBulletRow(
            label = "Create a new wallet",
            sub = "Or restore one you already have",
            isFirst = true,
        )
        OnbBulletRow(
            label = "One recovery phrase",
            sub = "Your 24-word BIP-39 phrase backs up both your wallet and messaging identity — store it offline.",
        )
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
) {
    OnbScreen(
        step = 3,
        ghostNum = 5,
        badge = "Part 2 · Wallet",
        cta = "",
        onCta = {},
        showBack = true,
        onBack = onBack,
        showCta = false,
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
        step = 4,
        title = "Wallet\nrecovery phrase",
        sub = "These words restore your funds and your messaging identity. Store them safely offline.",
        words = words,
        onBack = onBack,
        onContinue = onContinue,
    )
}

