@file:Suppress("TooManyFunctions")

package co.electriccoin.zcash.ui.screen.onboarding.view

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import kotlinx.coroutines.delay

// ───────────────────────────────────────────────────────────────
// 09 · Phase 3 / 2FA choice
// ───────────────────────────────────────────────────────────────

enum class TwoFAMode { Bio, Pin }

@Composable
fun TwoFAChoiceScreen(
    onBack: () -> Unit,
    onPick: (TwoFAMode) -> Unit,
) {
    OnbScreen(
        step = 6,
        ghostNum = 9,
        badge = "Part 3 of 3 · Secure Zapp",
        cta = "",
        onCta = {},
        showBack = true,
        onBack = onBack,
        showCta = false,
    ) {
        OnbHero(text = "Secure\nyour app")
        Spacer(Modifier.height(14.dp))
        OnbSub("Choose how you unlock Zapp and authorise payments.")
        Spacer(Modifier.height(28.dp))

        // Each tile carries its own 1dp border (no shared list-card frame).
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OnbActionRow(
                action = OnbAction(
                    icon = "◎",
                    label = "Biometric",
                    sub = "Fingerprint or face — fastest",
                    onClick = { onPick(TwoFAMode.Bio) },
                    highlight = true,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ZappTheme.colors.border, RectangleShape),
            )
            OnbActionRow(
                action = OnbAction(
                    icon = "✱",
                    label = "6-digit PIN",
                    sub = "A passcode you remember",
                    onClick = { onPick(TwoFAMode.Pin) },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ZappTheme.colors.border, RectangleShape),
            )
        }
    }
}

// ───────────────────────────────────────────────────────────────
// 10 · Biometric setup
// ───────────────────────────────────────────────────────────────

/**
 * Real biometric enrollment screen.
 *
 * @param isEnrolling True while the system biometric prompt is in flight — disables CTA.
 * @param errorMessage Non-null when the last attempt failed; shown in danger color.
 * @param onEnroll Called when the user taps the CTA (or "Retry" on error).
 * @param onCancel Called when the user taps back — returns to the choice screen.
 */
@Composable
fun BioScanScreen(
    isEnrolling: Boolean,
    errorMessage: String?,
    onEnroll: () -> Unit,
    onCancel: () -> Unit,
) {
    val c = ZappTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 20.dp)) {
            OnbProgress(step = 6)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 28.dp),
            ) {
                BasicText(
                    text = "◉",
                    style = ZappTheme.typography.display.copy(
                        color = when {
                            isEnrolling -> c.accent
                            errorMessage != null -> c.danger
                            else -> c.text
                        },
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                    ),
                )
                Spacer(Modifier.height(20.dp))
                BasicText(
                    text = when {
                        isEnrolling -> "Verifying…"
                        else -> "Biometric unlock"
                    },
                    style = ZappTheme.typography.display.copy(
                        color = c.text,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.8).sp,
                    ),
                )
                Spacer(Modifier.height(8.dp))
                if (errorMessage != null) {
                    BasicText(
                        text = errorMessage,
                        style = ZappTheme.typography.body.copy(
                            color = c.danger,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                        ),
                        modifier = Modifier.fillMaxWidth(0.9f),
                    )
                } else {
                    OnbSub(
                        text = "Zapp will use your fingerprint or face to lock and unlock the app.",
                        modifier = Modifier.fillMaxWidth(0.9f),
                    )
                }
            }
        }
        OnbBottomDock(
            cta = when {
                isEnrolling -> "Verifying…"
                errorMessage != null -> "Retry"
                else -> "Enable Biometrics"
            },
            onCta = onEnroll,
            ctaEnabled = !isEnrolling,
            showBack = true,
            onBack = onCancel,
        )
    }
}

// ───────────────────────────────────────────────────────────────
// PIN verification (shared across app-open, send, seed reveal)
// ───────────────────────────────────────────────────────────────

/**
 * Single-phase PIN entry for authentication verification.
 *
 * Submits the PIN automatically once 6 digits are entered. Clears the input
 * immediately on submission. When [hasError] transitions to true (wrong PIN),
 * the input is reset and an error indicator is shown so the user can retry.
 *
 * @param hasError True while the most-recent submission failed; drives the error
 *   dot colour and clears the input.
 * @param showBack Whether to show a back / cancel button at the bottom. Pass
 *   `false` for mandatory auth gates (e.g. app-open) where the user cannot skip.
 * @param onPinSubmit Called with the 6-digit string once the user completes entry.
 * @param onCancel Called when the user cancels (navigates back). Only relevant
 *   when [showBack] is true.
 */
@Composable
fun PinVerifyScreen(
    hasError: Boolean,
    showBack: Boolean = true,
    onPinSubmit: (String) -> Unit,
    onCancel: () -> Unit = {},
) {
    val c = ZappTheme.colors
    var currentInput by rememberSaveable { mutableStateOf("") }

    // Clear input whenever an error is signalled so the user starts fresh.
    LaunchedEffect(hasError) {
        if (hasError) currentInput = ""
    }

    // Auto-submit when 6 digits are entered.
    LaunchedEffect(currentInput) {
        if (currentInput.length == 6) {
            val pin = currentInput
            currentInput = ""
            onPinSubmit(pin)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 28.dp, end = 28.dp, top = 24.dp),
        ) {
            Column(modifier = Modifier.align(Alignment.TopStart).fillMaxWidth()) {
                Spacer(Modifier.height(14.dp))
                OnbHero(text = "Enter\nyour PIN")
                Spacer(Modifier.height(14.dp))
                if (hasError) {
                    BasicText(
                        text = "Incorrect PIN. Please try again.",
                        style = ZappTheme.typography.body.copy(
                            color = c.danger,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                        ),
                    )
                } else {
                    OnbSub(text = "Enter your 6-digit PIN to continue.")
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PinDotRow(filledCount = currentInput.length, hasError = hasError)
                Spacer(Modifier.height(28.dp))
                PinKeypad(
                    modifier = Modifier.fillMaxWidth(),
                    onKey = { key ->
                        when {
                            key == "⌫" -> if (currentInput.isNotEmpty()) currentInput = currentInput.dropLast(1)
                            currentInput.length < 6 -> currentInput += key
                        }
                    },
                )
            }
        }
        if (showBack) {
            OnbBottomDock(
                cta = "",
                onCta = {},
                showBack = true,
                onBack = onCancel,
                showCta = false,
            )
        }
    }
}

// ───────────────────────────────────────────────────────────────
// 10b · 6-digit PIN setup
// ───────────────────────────────────────────────────────────────

/**
 * Two-phase 6-digit PIN creation screen.
 *
 * Phase 1 — user enters a new PIN.
 * Phase 2 — user re-enters the same PIN to confirm.
 * Mismatch: error shown for 1.5 s, then resets to phase 1.
 * On match: [onPinConfirmed] is called with the raw 6-digit string.
 */
@Composable
fun PinSetupScreen(
    onBack: () -> Unit,
    onPinConfirmed: (String) -> Unit,
) {
    val c = ZappTheme.colors

    var isConfirmPhase by rememberSaveable { mutableStateOf(false) }
    var firstPin by rememberSaveable { mutableStateOf("") }
    var currentInput by rememberSaveable { mutableStateOf("") }
    var mismatchError by rememberSaveable { mutableStateOf(false) }

    // Auto-advance phases when 6 digits are entered.
    LaunchedEffect(currentInput) {
        if (currentInput.length == 6) {
            if (!isConfirmPhase) {
                firstPin = currentInput
                currentInput = ""
                isConfirmPhase = true
            } else {
                if (currentInput == firstPin) {
                    onPinConfirmed(currentInput)
                } else {
                    mismatchError = true
                    currentInput = ""
                }
            }
        }
    }

    // Auto-reset after mismatch so the user starts over from phase 1.
    LaunchedEffect(mismatchError) {
        if (mismatchError) {
            delay(1500)
            isConfirmPhase = false
            firstPin = ""
            currentInput = ""
            mismatchError = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 20.dp)) {
            OnbProgress(step = 6)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 28.dp, end = 28.dp, top = 24.dp),
        ) {
            GhostNum(n = 9, modifier = Modifier.align(Alignment.TopEnd))
            // Hero text at the top
            Column(modifier = Modifier.align(Alignment.TopStart).fillMaxWidth()) {
                Eyebrow("Part 3 of 3 · Secure Zapp")
                Spacer(Modifier.height(14.dp))
                OnbHero(
                    text = if (isConfirmPhase) "Confirm\nyour PIN" else "Create\nyour PIN"
                )
                Spacer(Modifier.height(14.dp))
                if (mismatchError) {
                    BasicText(
                        text = "PINs don't match. Please try again.",
                        style = ZappTheme.typography.body.copy(
                            color = c.danger,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                        ),
                    )
                } else {
                    OnbSub(
                        text = if (isConfirmPhase)
                            "Re-enter your 6-digit PIN to confirm."
                        else
                            "Choose a 6-digit code you'll remember.",
                    )
                }
            }
            // Dots + keypad anchored to the bottom of the body area
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PinDotRow(filledCount = currentInput.length, hasError = mismatchError)
                Spacer(Modifier.height(28.dp))
                PinKeypad(
                    modifier = Modifier.fillMaxWidth(),
                    onKey = { key ->
                        if (!mismatchError) {
                            when {
                                key == "⌫" -> if (currentInput.isNotEmpty()) {
                                    currentInput = currentInput.dropLast(1)
                                }
                                currentInput.length < 6 -> currentInput += key
                            }
                        }
                    },
                )
            }
        }
        // Back at bottom-left, no CTA — PIN completion auto-advances.
        OnbBottomDock(
            cta = "",
            onCta = {},
            showBack = true,
            onBack = {
                if (isConfirmPhase) {
                    isConfirmPhase = false
                    currentInput = ""
                    firstPin = ""
                } else {
                    onBack()
                }
            },
            showCta = false,
        )
    }
}

/** Six square dots that fill as the user enters each digit. */
@Composable
private fun PinDotRow(filledCount: Int, hasError: Boolean) {
    val c = ZappTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(6) { i ->
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(
                        when {
                            hasError -> c.danger
                            i < filledCount -> c.text
                            else -> c.border
                        },
                        RectangleShape,
                    ),
            )
        }
    }
}

/** Standard phone-layout numeric keypad (1-9, blank, 0, ⌫). */
@Composable
private fun PinKeypad(modifier: Modifier = Modifier, onKey: (String) -> Unit) {
    val c = ZappTheme.colors
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(null, "0", "⌫"),
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    if (key == null) {
                        Box(modifier = Modifier.weight(1f).height(60.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .border(1.dp, c.border, RectangleShape)
                                .clickable(onClick = { onKey(key) }),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = key,
                                style = ZappTheme.typography.button.copy(
                                    color = c.text,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────────────────────────────────────────────
// 11 · Done
// ───────────────────────────────────────────────────────────────

@Composable
fun OnboardingDoneScreen(
    mode: TwoFAMode,
    onEnter: () -> Unit,
) {
    val c = ZappTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BasicText(
                    text = "✓",
                    style = ZappTheme.typography.display.copy(
                        color = c.accent,
                        fontSize = 88.sp,
                        lineHeight = 92.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-4).sp,
                    ),
                )
                Spacer(Modifier.height(18.dp))
                BasicText(
                    text = "You're\nall set.",
                    style = ZappTheme.typography.display.copy(
                        color = c.text,
                        fontSize = 42.sp,
                        lineHeight = 44.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.8).sp,
                    ),
                )
                Spacer(Modifier.height(20.dp))
                AccentRule()
                Spacer(Modifier.height(20.dp))
                OnbSub(
                    text = "Identity created, wallet ready" + when (mode) {
                        TwoFAMode.Bio -> ", secured with biometrics."
                        TwoFAMode.Pin -> ", secured with a PIN."
                    },
                )
            }
        }
        OnbBottomDock(cta = "Enter Zapp →", onCta = onEnter)
    }
}
