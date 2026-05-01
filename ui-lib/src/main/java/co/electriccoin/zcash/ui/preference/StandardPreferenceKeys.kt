package co.electriccoin.zcash.ui.preference

import co.electriccoin.zcash.preference.model.entry.BooleanPreferenceDefault
import co.electriccoin.zcash.preference.model.entry.IntegerPreferenceDefault
import co.electriccoin.zcash.preference.model.entry.LongPreferenceDefault
import co.electriccoin.zcash.preference.model.entry.PreferenceKey
import co.electriccoin.zcash.ui.common.model.AppLockMode
import co.electriccoin.zcash.ui.common.model.OnboardingState

object StandardPreferenceKeys {
    /**
     * State defining whether the user has completed any of the onboarding wallet states.
     */
    val ONBOARDING_STATE =
        IntegerPreferenceDefault(
            PreferenceKey("onboarding_state"),
            OnboardingState.NONE.toNumber()
        )

    val IS_BACKGROUND_SYNC_ENABLED = BooleanPreferenceDefault(PreferenceKey("is_background_sync_enabled"), true)

    /**
     * True once the first-launch welcome gate has been dismissed (the user tapped
     * "Get started" or "I already use Zapp"). Drives whether `WelcomeGateView` or
     * the tabs scaffold is shown.
     */
    val IS_WELCOME_DISMISSED =
        BooleanPreferenceDefault(
            PreferenceKey("is_welcome_dismissed"),
            false
        )

    /**
     * True once the user has finished the Swiss-design onboarding flow (Phase
     * intro → username → seed phrases → 2FA → Done). Independent of the Zashi
     * `ONBOARDING_STATE` so that "skip wallet" users can complete onboarding
     * without ever creating a wallet.
     */
    val IS_ONBOARDING_COMPLETED =
        BooleanPreferenceDefault(
            PreferenceKey("is_onboarding_completed"),
            false
        )

    /**
     * How the app gates access on launch. Defaults to [AppLockMode.NONE] so a
     * device with no biometrics enrolled doesn't trip the BiometricPrompt no-op
     * before the user has a chance to set up a PIN during onboarding. The
     * onboarding flow always writes the chosen mode (NONE, BIOMETRIC, or PIN)
     * before [IS_ONBOARDING_COMPLETED] flips, so the gate never sees the
     * default in production.
     */
    val APP_LOCK_MODE =
        IntegerPreferenceDefault(
            PreferenceKey("APP_LOCK_MODE"),
            AppLockMode.NONE.toNumber()
        )

    val IS_HIDE_BALANCES =
        BooleanPreferenceDefault(
            PreferenceKey("IS_HIDE_BALANCES"),
            false
        )
    val LATEST_APP_BACKGROUND_TIME_MILLIS =
        LongPreferenceDefault(
            PreferenceKey("LATEST_APP_BACKGROUND_TIME_MILLIS"),
            Long.MAX_VALUE
        )
}
