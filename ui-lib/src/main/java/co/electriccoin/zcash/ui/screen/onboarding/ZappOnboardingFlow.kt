package co.electriccoin.zcash.ui.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.electriccoin.zcash.ui.NavigationRouter
import co.electriccoin.zcash.ui.common.model.AppLockMode
import co.electriccoin.zcash.ui.common.viewmodel.SecretState
import co.electriccoin.zcash.ui.common.viewmodel.WalletViewModel
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel
import co.electriccoin.zcash.ui.screen.onboarding.view.BioScanScreen
import co.electriccoin.zcash.ui.screen.onboarding.view.MessagingPhaseIntro
import co.electriccoin.zcash.ui.screen.onboarding.view.OnboardingDoneScreen
import co.electriccoin.zcash.ui.screen.onboarding.view.TwoFAChoiceScreen
import co.electriccoin.zcash.ui.screen.onboarding.view.TwoFAMode
import co.electriccoin.zcash.ui.screen.onboarding.view.UsernameEntryScreen
import co.electriccoin.zcash.ui.screen.onboarding.view.WalletChoiceScreen
import co.electriccoin.zcash.ui.screen.onboarding.view.WalletPhaseIntro
import co.electriccoin.zcash.ui.screen.onboarding.view.WalletSeedPhraseScreen
import co.electriccoin.zcash.ui.screen.pin.PinSetupScreen
import co.electriccoin.zcash.ui.screen.restore.seed.RestoreSeedArgs
import co.electriccoin.zcash.ui.screen.welcome.WelcomeGateVM
import org.koin.androidx.compose.koinViewModel

/** All steps the Swiss onboarding flow walks the user through. */
private enum class Step {
    MSG_INTRO,
    MSG_USERNAME,
    WALLET_INTRO,
    WALLET_CHOICE,
    WALLET_SEED,
    SECURE_CHOICE,
    BIO_SCAN,
    PIN_SETUP,
    DONE,
}

/**
 * Swiss-design post-welcome onboarding orchestrator.
 *
 * Runs after [co.electriccoin.zcash.ui.screen.welcome.view.WelcomeGateView] is
 * dismissed and before the user reaches the tabs shell. Three phases mirror the
 * design canvas:
 * - **Part 1 — Messaging account** (intro, username)
 * - **Part 2 — Wallet** (intro, create/restore/skip, seed)
 * - **Part 3 — Secure Zapp** (biometric/PIN, scan, done)
 *
 * The wallet's 24-word BIP-39 phrase seeds the messaging identity via
 * [ChatViewModel.restoreFromWalletSeed], so users back up one phrase for
 * everything. Wallet restore forwards to the existing [RestoreSeedArgs] flow;
 * an observer auto-advances to the secure step once `secretState` flips to
 * READY so the user lands back inside onboarding rather than on an unfinished
 * tabs shell.
 */
@Composable
fun ZappOnboardingFlow(
    onComplete: () -> Unit,
    walletViewModel: WalletViewModel,
    chatViewModel: ChatViewModel,
    navigationRouter: NavigationRouter,
) {
    var step by rememberSaveable { mutableStateOf(Step.MSG_INTRO) }
    var twoFAMode by rememberSaveable { mutableStateOf(TwoFAMode.Pin) }
    var pendingUsername by rememberSaveable { mutableStateOf("") }

    val welcomeGateVM = koinViewModel<WelcomeGateVM>()

    val walletSeed by walletViewModel.currentSeedWords.collectAsStateWithLifecycle()
    val secretState by walletViewModel.secretState.collectAsStateWithLifecycle()

    // Wallet-restore lives outside this composable (RestoreSeedScreen). When it
    // completes, secretState flips to READY and the user is popped back into
    // the tabs scaffold, which re-renders this flow at the saved WALLET_CHOICE
    // step. Auto-advance so they don't have to re-tap "Restore" / "Create".
    // Effect is keyed only on secretState — step is checked inside but doesn't
    // need to retrigger the effect on its own changes.
    LaunchedEffect(secretState) {
        if (secretState == SecretState.READY && step == Step.WALLET_CHOICE) {
            step = Step.SECURE_CHOICE
        }
    }

    when (step) {
        Step.MSG_INTRO -> MessagingPhaseIntro(
            onBack = onComplete, // back from first step exits onboarding
            onContinue = { step = Step.MSG_USERNAME },
        )
        Step.MSG_USERNAME -> UsernameEntryScreen(
            onBack = { step = Step.MSG_INTRO },
            onContinue = { name ->
                pendingUsername = name
                step = Step.WALLET_INTRO
            },
        )
        Step.WALLET_INTRO -> WalletPhaseIntro(
            onBack = { step = Step.MSG_USERNAME },
            onContinue = { step = Step.WALLET_CHOICE },
        )
        Step.WALLET_CHOICE -> WalletChoiceScreen(
            onBack = { step = Step.WALLET_INTRO },
            onCreate = {
                walletViewModel.createNewWallet()
                chatViewModel.restoreFromWalletSeed(pendingUsername)
                step = Step.WALLET_SEED
            },
            onRestore = {
                navigationRouter.forward(RestoreSeedArgs)
            },
        )
        Step.WALLET_SEED -> {
            val words = walletSeed
            if (words == null) {
                // Wallet creation has no error pipe today — fall back to a
                // timeout so the user isn't stuck on a spinner if persistence
                // silently stalls.
                SeedLoadingPlaceholder(sdkError = null)
            } else {
                WalletSeedPhraseScreen(
                    words = words,
                    onBack = { step = Step.WALLET_CHOICE },
                    onContinue = { step = Step.SECURE_CHOICE },
                )
            }
        }
        Step.SECURE_CHOICE -> TwoFAChoiceScreen(
            onBack = { step = Step.WALLET_INTRO },
            onPick = { mode ->
                twoFAMode = mode
                step = when (mode) {
                    TwoFAMode.Bio -> Step.BIO_SCAN
                    TwoFAMode.Pin -> Step.PIN_SETUP
                    TwoFAMode.None -> {
                        welcomeGateVM.setAppLockMode(AppLockMode.NONE)
                        Step.DONE
                    }
                }
            },
        )
        Step.BIO_SCAN -> BioScanScreen(
            onCancel = { step = Step.SECURE_CHOICE },
            onDone = {
                welcomeGateVM.setAppLockMode(AppLockMode.BIOMETRIC)
                step = Step.DONE
            },
        )
        Step.PIN_SETUP -> PinSetupScreen(
            onBack = { step = Step.SECURE_CHOICE },
            onComplete = {
                welcomeGateVM.setAppLockMode(AppLockMode.PIN)
                step = Step.DONE
            },
        )
        Step.DONE -> OnboardingDoneScreen(
            mode = twoFAMode,
            onEnter = onComplete,
        )
    }
}

private const val SEED_LOAD_TIMEOUT_MS = 15_000L

/**
 * Brief skeleton shown while the SDK finishes generating the recovery phrase
 * (chat) or persisting the new wallet (wallet). On the happy path this flashes
 * for under a second; if [sdkError] is non-null we surface it immediately. If
 * the SDK doesn't error but also doesn't finish within [SEED_LOAD_TIMEOUT_MS],
 * we fall back to a generic message so the user isn't trapped on a spinner.
 */
@Composable
private fun SeedLoadingPlaceholder(sdkError: String?) {
    val c = ZappTheme.colors
    var timedOut by remember { mutableStateOf(false) }

    LaunchedEffect(sdkError) {
        if (sdkError == null) {
            kotlinx.coroutines.delay(SEED_LOAD_TIMEOUT_MS)
            timedOut = true
        }
    }

    val displayError = sdkError
        ?: "Taking longer than expected.".takeIf { timedOut }

    Box(
        modifier = Modifier.fillMaxSize().background(c.bg),
        contentAlignment = Alignment.Center,
    ) {
        if (displayError != null) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BasicText(
                    text = displayError,
                    style = ZappTheme.typography.body.copy(
                        color = c.danger,
                        fontSize = 13.sp,
                    ),
                )
                Spacer(Modifier.height(12.dp))
                BasicText(
                    text = "Try going back and submitting again.",
                    style = ZappTheme.typography.body.copy(
                        color = c.textMuted,
                        fontSize = 12.sp,
                    ),
                )
            }
        } else {
            CircularProgressIndicator(color = c.accent)
        }
    }
}
