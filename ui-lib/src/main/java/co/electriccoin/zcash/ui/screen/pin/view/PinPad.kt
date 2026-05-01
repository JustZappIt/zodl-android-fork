package co.electriccoin.zcash.ui.screen.pin.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.electriccoin.zcash.ui.design.theme.ZappTheme

internal const val PIN_LENGTH = 6

/**
 * Six-dot indicator + 3x4 numeric keypad. Pure UI: callers own the `value`
 * state and consume digit/backspace events. Used by both PIN setup (twice —
 * create + confirm) and PIN unlock so the visual treatment stays identical.
 */
@Composable
fun PinPad(
    value: String,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PinDots(filled = value.length)
        Spacer(Modifier.height(28.dp))
        PinKeypad(onDigit = onDigit, onBackspace = onBackspace, enabled = enabled)
    }
}

@Composable
private fun PinDots(filled: Int) {
    val c = ZappTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
    ) {
        repeat(PIN_LENGTH) { i ->
            val active = i < filled
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(if (active) c.accent else c.bg, RectangleShape)
                    .border(2.dp, if (active) c.accent else c.borderStrong, RectangleShape),
            )
        }
    }
}

@Composable
private fun PinKeypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    enabled: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "⌫"),
        ).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                row.forEach { label ->
                    Box(modifier = Modifier.weight(1f)) {
                        when (label) {
                            "" -> Spacer(modifier = Modifier.height(54.dp))
                            "⌫" -> KeypadKey(label = label, enabled = enabled, onClick = onBackspace)
                            else -> KeypadKey(
                                label = label,
                                enabled = enabled,
                                onClick = { onDigit(label[0]) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val c = ZappTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(c.surfaceAlt, RectangleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = ZappTheme.typography.body.copy(
                color = c.text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}
