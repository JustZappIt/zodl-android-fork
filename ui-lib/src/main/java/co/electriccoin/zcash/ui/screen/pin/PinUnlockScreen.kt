package co.electriccoin.zcash.ui.screen.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.screen.pin.view.PinScreen
import org.koin.androidx.compose.koinViewModel

/**
 * App-access PIN gate. Shown on launch when [co.electriccoin.zcash.ui.common.model.AppLockMode.PIN]
 * is the configured lock mode. Calls [onSuccess] once the user enters the
 * correct PIN; lockouts and attempt counts are owned by [PinUnlockVM] and the
 * underlying [co.electriccoin.zcash.ui.common.repository.PinRepository].
 *
 * [onForgot] is the destructive escape hatch — invoked after the user confirms
 * a "reset wallet" prompt. If null, the forgot-PIN dock is hidden (no other
 * recovery path is offered).
 */
@Composable
fun PinUnlockScreen(
    onSuccess: () -> Unit,
    onForgot: (() -> Unit)? = null,
) {
    val vm: PinUnlockVM = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    var showForgotConfirm by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is PinUnlockEvent.Success -> onSuccess()
            }
        }
    }

    val secondsRemaining = state.lockoutSecondsRemaining()
    val errorText = when {
        secondsRemaining > 0 -> "Locked. Try again in ${secondsRemaining}s."
        else -> state.error
    }

    PinScreen(
        title = "Enter PIN",
        subtitle = "Unlock Zapp to continue.",
        value = state.value,
        onDigit = vm::onDigit,
        onBackspace = vm::onBackspace,
        onBack = onForgot?.let { { showForgotConfirm = true } },
        backLabel = "Forgot PIN? Reset wallet",
        errorText = errorText,
        enabled = !state.isBusy && secondsRemaining == 0,
    )

    if (showForgotConfirm && onForgot != null) {
        ForgotPinConfirm(
            onCancel = { showForgotConfirm = false },
            onConfirm = {
                showForgotConfirm = false
                onForgot()
            },
        )
    }
}

@Composable
private fun ForgotPinConfirm(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val c = ZappTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg.copy(alpha = 0.96f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicText(
                text = "Reset wallet?",
                style = ZappTheme.typography.display.copy(
                    color = c.text,
                    fontSize = 26.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                ),
            )
            Spacer(Modifier.height(12.dp))
            BasicText(
                text = "This permanently erases your wallet, chats, and PIN on this device. " +
                    "You'll need your recovery phrase to restore.",
                style = ZappTheme.typography.body.copy(
                    color = c.textMuted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                ),
            )
            Spacer(Modifier.height(24.dp))
            ConfirmButton(
                label = "Erase and reset",
                background = c.danger,
                foreground = c.bg,
                onClick = onConfirm,
            )
            Spacer(Modifier.height(8.dp))
            ConfirmButton(
                label = "Cancel",
                background = c.surfaceAlt,
                foreground = c.text,
                borderColor = c.borderStrong,
                onClick = onCancel,
            )
        }
    }
}

@Composable
private fun ConfirmButton(
    label: String,
    background: Color,
    foreground: Color,
    onClick: () -> Unit,
    borderColor: Color? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(background, RectangleShape)
            .let { if (borderColor != null) it.border(2.dp, borderColor, RectangleShape) else it }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = ZappTheme.typography.body.copy(
                color = foreground,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}
