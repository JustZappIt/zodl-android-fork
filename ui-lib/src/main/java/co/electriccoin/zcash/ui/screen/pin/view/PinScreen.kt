package co.electriccoin.zcash.ui.screen.pin.view

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.screen.onboarding.view.OnbBottomDock

/**
 * Common scaffold for PIN flows (setup, confirm, unlock). Bound to the same
 * Swiss-design tokens as the rest of onboarding so the keypad doesn't visually
 * jar against [SecurePhaseView]. Callers wire title/subtitle and the active
 * PIN buffer; this composable only handles layout.
 */
@Composable
@Suppress("LongParameterList")
fun PinScreen(
    title: String,
    subtitle: String,
    value: String,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onBack: (() -> Unit)?,
    backLabel: String = "Back",
    errorText: String? = null,
    enabled: Boolean = true,
) {
    val c = ZappTheme.colors
    SecureWindow()
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
                    text = title,
                    style = ZappTheme.typography.display.copy(
                        color = c.text,
                        fontSize = 30.sp,
                        lineHeight = 32.sp,
                        letterSpacing = (-0.8).sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                    ),
                )
                Spacer(Modifier.height(10.dp))
                BasicText(
                    text = subtitle,
                    style = ZappTheme.typography.body.copy(
                        color = c.textMuted,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                    ),
                )
                Spacer(Modifier.height(28.dp))
                PinPad(
                    value = value,
                    onDigit = onDigit,
                    onBackspace = onBackspace,
                    enabled = enabled,
                )
                if (errorText != null) {
                    Spacer(Modifier.height(14.dp))
                    BasicText(
                        text = errorText,
                        style = ZappTheme.typography.body.copy(
                            color = c.danger,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
        }
        if (onBack != null) {
            OnbBottomDock(cta = backLabel, onCta = onBack, noBorder = true)
        }
    }
}

/**
 * Sets [WindowManager.LayoutParams.FLAG_SECURE] while the PIN screen is on
 * screen. Blocks screenshots, screen recording, and the recents-app preview
 * from capturing PIN entry. The flag is cleared on dispose.
 */
@Composable
private fun SecureWindow() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
