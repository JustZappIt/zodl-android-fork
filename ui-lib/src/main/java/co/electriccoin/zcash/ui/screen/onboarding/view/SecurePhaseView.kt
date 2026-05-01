@file:Suppress("TooManyFunctions")

package co.electriccoin.zcash.ui.screen.onboarding.view

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import kotlinx.coroutines.delay

// ───────────────────────────────────────────────────────────────
// 09 · Phase 3 / 2FA choice
// ───────────────────────────────────────────────────────────────

enum class TwoFAMode { Bio, Pin, None }

@Composable
fun TwoFAChoiceScreen(
    onBack: () -> Unit,
    onPick: (TwoFAMode) -> Unit,
) {
    OnbScreen(
        step = 6,
        ghostNum = 9,
        badge = "Part 3 of 3 · Secure Zapp",
        cta = "Skip — set up later",
        onCta = { onPick(TwoFAMode.None) },
        showBack = true,
        onBack = onBack,
    ) {
        OnbHero(text = "Secure\nyour app")
        Spacer(Modifier.height(14.dp))
        OnbSub("Choose how you unlock Zapp and authorise payments.")
        Spacer(Modifier.height(28.dp))

        // Each tile carries its own 1dp border (no shared list-card frame), so
        // we render a Column of standalone OnbActionRows rather than wrapping
        // them in OnbActionListCard. Same row treatment, different chrome.
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
// 10 · Bio scan
// ───────────────────────────────────────────────────────────────

@Composable
fun BioScanScreen(
    onCancel: () -> Unit,
    onDone: () -> Unit,
) {
    val c = ZappTheme.colors
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "scan",
    )

    LaunchedEffect(Unit) {
        repeat(20) {
            delay(80)
            progress = (it + 1) / 20f
        }
        delay(150)
        onDone()
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
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Square)
                        val r = size.minDimension / 2 - stroke.width
                        val topLeft = Offset((size.width - r * 2) / 2, (size.height - r * 2) / 2)
                        val s = Size(r * 2, r * 2)
                        drawArc(
                            color = c.border,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = s,
                            style = stroke,
                        )
                        drawArc(
                            color = c.accent,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = s,
                            style = stroke,
                        )
                    }
                    BasicText(
                        text = "◉",
                        style = ZappTheme.typography.display.copy(
                            color = c.accent,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Black,
                        ),
                    )
                }
                Spacer(Modifier.height(20.dp))
                BasicText(
                    text = "Scanning",
                    style = ZappTheme.typography.display.copy(
                        color = c.text,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.8).sp,
                    ),
                )
                Spacer(Modifier.height(6.dp))
                OnbSub("Hold your finger on the sensor")
            }
        }
        OnbBottomDock(cta = "Cancel", onCta = onCancel, noBorder = true)
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
                    text = "Identity created, wallet ready, secured with " +
                        when (mode) {
                            TwoFAMode.Bio -> "biometrics."
                            TwoFAMode.Pin -> "a PIN."
                            TwoFAMode.None -> "no app lock — set one in Settings."
                        },
                )
            }
        }
        OnbBottomDock(cta = "Enter Zapp →", onCta = onEnter)
    }
}

