package co.electriccoin.zcash.ui.screen.request.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.z.ecc.android.sdk.model.WalletAddress
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.design.component.ZashiQr
import co.electriccoin.zcash.ui.design.component.zapp.ZappChipVariant
import co.electriccoin.zcash.ui.design.component.zapp.ZappStatusChip
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.design.util.stringRes
import co.electriccoin.zcash.ui.screen.request.model.RequestState

@Composable
internal fun RequestQrCodeView(
    state: RequestState.QrCode,
    modifier: Modifier = Modifier
) {
    val c = ZappTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
    ) {
        Spacer(Modifier.height(20.dp))

        when (state.walletAddress) {
            is WalletAddress.Transparent -> ZappStatusChip(
                text = stringResource(id = R.string.request_privacy_level_transparent).uppercase(),
                variant = ZappChipVariant.Accent,
                dotColor = c.accent,
            )
            is WalletAddress.Unified, is WalletAddress.Sapling -> ZappStatusChip(
                text = stringResource(id = R.string.request_privacy_level_shielded).uppercase(),
                variant = ZappChipVariant.Success,
                dotColor = c.success,
            )
            else -> error("Unsupported address type")
        }

        Spacer(Modifier.height(20.dp))

        RequestQrCodeZecAmountView(state = state)

        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .background(c.bg, RectangleShape)
                .border(BorderStroke(1.dp, c.border), RectangleShape)
                .padding(12.dp),
        ) {
            QrCode(requestState = state)
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun QrCode(
    requestState: RequestState.QrCode,
    modifier: Modifier = Modifier
) {
    ZashiQr(
        state = requestState.toQrState(
            contentDescription = stringRes(R.string.request_qr_code_content_description),
            centerImage = requestState.icon,
        ),
        modifier = modifier,
    )
}

@Composable
private fun RequestQrCodeZecAmountView(
    state: RequestState.QrCode,
    modifier: Modifier = Modifier
) {
    val c = ZappTheme.colors
    val zecText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = c.text)) {
            append(state.request.qrCodeState.zecAmount)
        }
        append("\u2009")
        withStyle(style = SpanStyle(color = c.textMuted)) {
            append(state.zcashCurrency.localizedName(LocalContext.current))
        }
    }

    AutoSizingText(
        text = zecText,
        style = ZappTheme.typography.display.copy(
            color = c.text,
            fontSize = 44.sp,
            lineHeight = 48.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1.8).sp,
        ),
        modifier = modifier.fillMaxWidth(),
    )

    if (state.request.qrCodeState.memo.isNotBlank()) {
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, c.border), RectangleShape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Column {
                BasicText(
                    text = "MEMO",
                    style = ZappTheme.typography.eyebrow.copy(
                        color = c.textSubtle,
                        fontSize = 10.sp,
                        letterSpacing = 1.8.sp,
                        fontWeight = FontWeight.Black,
                    ),
                )
                Spacer(Modifier.height(4.dp))
                BasicText(
                    text = state.request.qrCodeState.memo,
                    style = ZappTheme.typography.body.copy(
                        color = c.textMuted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    ),
                )
            }
        }
    }
}
