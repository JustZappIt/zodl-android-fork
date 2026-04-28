@file:Suppress("TooManyFunctions")

package co.electriccoin.zcash.ui.screen.request.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.z.ecc.sdk.type.ZcashCurrency
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.common.wallet.ExchangeRateState
import co.electriccoin.zcash.ui.design.component.CircularScreenProgressIndicator
import co.electriccoin.zcash.ui.design.component.QrCodeDefaults
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.newcomponent.PreviewScreens
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.design.theme.ZcashTheme
import co.electriccoin.zcash.ui.screen.request.model.AmountState
import co.electriccoin.zcash.ui.screen.request.model.MemoState
import co.electriccoin.zcash.ui.screen.request.model.QrCodeState
import co.electriccoin.zcash.ui.screen.request.model.Request
import co.electriccoin.zcash.ui.screen.request.model.RequestCurrency
import co.electriccoin.zcash.ui.screen.request.model.RequestState
import kotlin.math.roundToInt

@Composable
@PreviewScreens
private fun RequestLoadingPreview() =
    ZcashTheme(forceDarkMode = true) {
        RequestView(
            state = RequestState.Loading,
            snackbarHostState = SnackbarHostState(),
        )
    }

@Composable
@PreviewScreens
private fun RequestPreview() =
    ZcashTheme(forceDarkMode = false) {
        RequestView(
            state =
                RequestState.Amount(
                    request =
                        Request(
                            amountState = AmountState("2.25", RequestCurrency.ZEC, true),
                            memoState = MemoState.Valid("", 0, "2.25"),
                            qrCodeState =
                                QrCodeState(
                                    "zcash:t1duiEGg7b39nfQee3XaTY4f5McqfyJKhBi?amount=1&memo=VGhpcyBpcyBhIHNpbXBsZSBt",
                                    "0.25",
                                    memo = "Text memo",
                                ),
                        ),
                    exchangeRateState = ExchangeRateState.OptedOut,
                    zcashCurrency = ZcashCurrency.ZEC,
                    onAmount = {},
                    onSwitch = {},
                    onBack = {}
                ) {},
            snackbarHostState = SnackbarHostState(),
        )
    }

@Composable
internal fun RequestView(
    state: RequestState,
    snackbarHostState: SnackbarHostState,
) {
    when (state) {
        RequestState.Loading -> {
            CircularScreenProgressIndicator()
        }

        is RequestState.Prepared -> {
            Scaffold(
                topBar = {
                    ZappScreenHeader(title = stringResource(id = R.string.request_title))
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    RequestBottomBar(state = state)
                }
            ) { paddingValues ->
                RequestContents(
                    state = state,
                    modifier =
                        Modifier.padding(
                            top = paddingValues.calculateTopPadding(),
                            bottom = paddingValues.calculateBottomPadding()
                        ),
                )
            }
        }
    }
}

@Composable
private fun RequestBottomBar(
    state: RequestState.Prepared,
    modifier: Modifier = Modifier,
) {
    val c = ZappTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(c.bg)
            .border(BorderStroke(1.dp, c.text), RectangleShape)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        when (state) {
            is RequestState.Amount -> SwissDockRow(
                onBack = state.onBack,
                cta = stringResource(id = R.string.request_amount_btn),
                onCta = state.onDone,
                ctaEnabled = state.request.amountState.isValid == true,
            )

            is RequestState.Memo -> SwissDockRow(
                onBack = state.onBack,
                cta = stringResource(id = R.string.request_memo_btn),
                onCta = state.onDone,
                ctaEnabled = state.request.memoState.isValid(),
            )

            is RequestState.QrCode -> {
                val sizePixels = with(LocalDensity.current) { DEFAULT_QR_CODE_SIZE.toPx() }.roundToInt()
                val colors = QrCodeDefaults.colors()
                // QR step has two stacked CTAs (share + close), back arrow on
                // the left like the other steps.
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SwissBackBox(onClick = state.onBack)
                    Column(modifier = Modifier.weight(1f)) {
                        SwissPrimaryButton(
                            text = stringResource(id = R.string.request_qr_share_btn),
                            onClick = {
                                state.onQrCodeShare(colors, sizePixels, state.request.qrCodeState.requestUri)
                            },
                            enabled = true,
                        )
                        SwissGhostButton(
                            text = stringResource(id = R.string.request_qr_close_btn),
                            onClick = state.onClose,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwissDockRow(
    onBack: () -> Unit,
    cta: String,
    onCta: () -> Unit,
    ctaEnabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SwissBackBox(onClick = onBack)
        SwissPrimaryButton(
            text = cta,
            onClick = onCta,
            enabled = ctaEnabled,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SwissBackBox(onClick: () -> Unit) {
    val c = ZappTheme.colors
    Box(
        modifier = Modifier
            .size(width = 72.dp, height = 52.dp)
            .border(BorderStroke(1.dp, c.border), RectangleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = "←",
            style = ZappTheme.typography.button.copy(
                color = c.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
            ),
        )
    }
}

// Swiss primary button uses brand yellow (matches Receive / Advanced
// Settings accents) so the request CTA reads as on-brand.
private val RequestYellow = Color(0xFFFCBB1A)
private val RequestYellowText = Color(0xFF1A1100)

@Composable
private fun SwissPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val c = ZappTheme.colors
    val bg = if (enabled) RequestYellow else c.surfaceAlt
    val fg = if (enabled) RequestYellowText else c.textSubtle
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(bg, RectangleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text.uppercase(),
            style = ZappTheme.typography.button.copy(
                color = fg,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
            ),
        )
    }
}

@Composable
private fun SwissGhostButton(
    text: String,
    onClick: () -> Unit,
) {
    val c = ZappTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 20.dp, height = 1.dp)
                    .background(c.text, RectangleShape),
            )
            Spacer(Modifier.width(10.dp))
            BasicText(
                text = text.uppercase(),
                style = ZappTheme.typography.button.copy(
                    color = c.text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp,
                ),
            )
        }
    }
}

val DEFAULT_QR_CODE_SIZE = 320.dp

@Composable
private fun RequestContents(
    state: RequestState.Prepared,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        when (state) {
            is RequestState.Amount -> {
                RequestAmountView(state = state)
            }

            is RequestState.Memo -> {
                RequestMemoView(state = state)
            }

            is RequestState.QrCode -> {
                RequestQrCodeView(state = state)
            }
        }
    }
}

// TODO [#1635]: Learn AutoSizingText scale up
// TODO [#1635]: https://github.com/Electric-Coin-Company/zashi-android/issues/1635
@Composable
internal fun AutoSizingText(
    text: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    var fontSize by remember { mutableStateOf(style.fontSize) }

    Text(
        text = text,
        fontSize = fontSize,
        fontFamily = style.fontFamily,
        lineHeight = style.lineHeight,
        fontWeight = style.fontWeight,
        maxLines = 1,
        modifier = modifier,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowHeight) {
                fontSize = (fontSize.value - 1).sp
            } else {
                // We should make the text bigger again
            }
        }
    )
}
