@file:Suppress("TooManyFunctions")

package co.electriccoin.zcash.ui.screen.onboarding.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.design.MINIMAL_WEIGHT
import co.electriccoin.zcash.ui.design.component.ZashiButton
import co.electriccoin.zcash.ui.design.component.ZashiButtonDefaults
import co.electriccoin.zcash.ui.design.newcomponent.PreviewScreens
import co.electriccoin.zcash.ui.design.theme.ZcashTheme
import co.electriccoin.zcash.ui.design.theme.colors.ZashiColors
import co.electriccoin.zcash.ui.design.theme.dimensions.ZashiDimensions
import co.electriccoin.zcash.ui.design.theme.typography.ZashiTypography

// ───────────────────────────────────────────────
// Onboarding step enum
// ───────────────────────────────────────────────

enum class OnboardingStep {
    WELCOME,
    MSG_INTRO,
    MSG_USERNAME,
    MSG_SEED,
    WALLET_INTRO,
}

// ───────────────────────────────────────────────
// Entry composable — wires all steps together
// ───────────────────────────────────────────────

// TODO [#1001]: Screens in landscape mode
// TODO [#1001]: https://github.com/Electric-Coin-Company/zashi-android/issues/1001
@PreviewScreens
@Composable
private fun OnboardingComposablePreview() {
    ZcashTheme {
        Onboarding(
            onImportWallet = {},
            onCreateWallet = {}
        )
    }
}

/**
 * @param onImportWallet Callback when the user decides to import an existing wallet.
 * @param onCreateWallet Callback when the user decides to create a new wallet.
 */
@Composable
fun Onboarding(
    onImportWallet: () -> Unit,
    onCreateWallet: () -> Unit,
) {
    var step by rememberSaveable { mutableStateOf(OnboardingStep.WELCOME) }
    var pendingUsername by rememberSaveable { mutableStateOf("") }

    when (step) {
        OnboardingStep.WELCOME ->
            OnboardingWelcome(
                onCreateNew = { step = OnboardingStep.MSG_INTRO },
                onRestoreExisting = onImportWallet,
            )

        OnboardingStep.MSG_INTRO ->
            MessagingPhaseIntro(
                onBack = { step = OnboardingStep.WELCOME },
                onContinue = { step = OnboardingStep.MSG_USERNAME },
            )

        OnboardingStep.MSG_USERNAME ->
            UsernameEntryScreen(
                onBack = { step = OnboardingStep.MSG_INTRO },
                onContinue = { username ->
                    pendingUsername = username
                    step = OnboardingStep.MSG_SEED
                },
            )

        OnboardingStep.MSG_SEED ->
            MessagingSeedPhraseScreen(
                onBack = { step = OnboardingStep.MSG_USERNAME },
                onContinue = { step = OnboardingStep.WALLET_INTRO },
            )

        OnboardingStep.WALLET_INTRO ->
            WalletPhaseIntro(
                onBack = { step = OnboardingStep.MSG_SEED },
                onContinue = onCreateWallet,
            )
    }
}

// ───────────────────────────────────────────────
// Welcome screen
// ───────────────────────────────────────────────

@Composable
private fun OnboardingWelcome(
    onCreateNew: () -> Unit,
    onRestoreExisting: () -> Unit,
) {
    Scaffold { paddingValues ->
        Box(
            modifier =
                Modifier.background(
                    if (isSystemInDarkTheme()) {
                        @Suppress("MagicNumber")
                        Brush.verticalGradient(
                            .0f to ZashiColors.Surfaces.bgSecondary,
                            .5f to ZashiColors.Surfaces.bgTertiary,
                            0.75f to ZashiColors.Surfaces.bgPrimary,
                        )
                    } else {
                        @Suppress("MagicNumber")
                        Brush.verticalGradient(
                            .0f to ZashiColors.Surfaces.bgSecondary,
                            .5f to ZashiColors.Surfaces.bgTertiary,
                            0.75f to ZashiColors.Surfaces.bgPrimary,
                        )
                    }
                )
        ) {
            OnboardingMainContent(
                onCreateNew = onCreateNew,
                onRestoreExisting = onRestoreExisting,
                modifier =
                    Modifier
                        .padding(
                            top = paddingValues.calculateTopPadding() + ZashiDimensions.Spacing.spacing2xl,
                            bottom = paddingValues.calculateBottomPadding() + ZashiDimensions.Spacing.spacing4xl,
                            start = ZashiDimensions.Spacing.spacing3xl,
                            end = ZashiDimensions.Spacing.spacing3xl,
                        )
            )
        }
    }
}

@Composable
private fun OnboardingMainContent(
    onCreateNew: () -> Unit,
    onRestoreExisting: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Image(
            modifier = Modifier.size(203.dp, 164.dp),
            painter = painterResource(id = co.electriccoin.zcash.ui.design.R.drawable.app_logo),
            colorFilter = ColorFilter.tint(color = ZcashTheme.colors.secondaryColor),
            contentDescription = stringResource(R.string.zcash_logo_content_description),
        )

        Spacer(modifier = Modifier.height(ZashiDimensions.Spacing.spacing5xl))

        Text(
            text = stringResource(R.string.onboarding_header),
            style = ZashiTypography.textXl,
            textAlign = TextAlign.Center,
            color = ZashiColors.Text.textSecondary,
        )

        @Suppress("MagicNumber")
        Spacer(Modifier.weight(.75f))

        Spacer(modifier = Modifier.height(ZcashTheme.dimens.spacingDefault))

        Spacer(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(MINIMAL_WEIGHT)
        )

        // Restore CTA — ghost/outline style
        ZashiButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.onboarding_import_existing_wallet),
            onClick = onRestoreExisting,
            colors = ZashiButtonDefaults.tertiaryColors(),
        )

        Spacer(modifier = Modifier.height(ZashiDimensions.Spacing.spacingLg))

        // Primary create CTA
        ZashiButton(
            onClick = onCreateNew,
            text = stringResource(R.string.onboarding_create_new_wallet),
            modifier = Modifier.fillMaxWidth(),
            hapticFeedbackType = HapticFeedbackType.Confirm,
        )
    }
}
