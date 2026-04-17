package co.electriccoin.zcash.ui.design.theme.colors

import androidx.compose.ui.graphics.Color

/**
 * Raw Zapp brand palette ported from zapp-android `ui/theme/Color.kt`.
 * Use these directly on screens that need the Zapp look (nav pill, FABs,
 * badges, primary CTAs). Everything else should keep using [ZashiColors].
 */
object ZappPalette {
    val Primary = Color(0xFFFF9417)
    val Accent = Color(0xFFFFB866)

    val Background = Color(0xFFFFFFFF)
    val BackgroundDark = Color(0xFF000000)

    val SecondaryBackground = Color(0xFFF2F2F7)
    val SecondaryBackgroundDark = Color(0xFF1C1C1E)

    val TextPrimary = Color(0xFF000000)
    val TextPrimaryDark = Color(0xFFFFFFFF)

    val TextSecondary = Color(0xFF3C3C43).copy(alpha = 0.6f)
    val TextSecondaryDark = Color(0xFFEBEBF5).copy(alpha = 0.6f)

    val TextTertiary = Color(0xFF3C3C43).copy(alpha = 0.3f)
    val TextTertiaryDark = Color(0xFFEBEBF5).copy(alpha = 0.3f)

    val Border = Color(0xFF3C3C43).copy(alpha = 0.36f)
    val BorderDark = Color(0xFF545458).copy(alpha = 0.65f)

    val Error = Color(0xFFFF3B30)
    val Success = Color(0xFF34C759)
    val Warning = Color(0xFFFF9500)

    val OnPrimary = Color(0xFFFFFFFF)
    val CardShadow = Color(0x0D000000)
}

object ZappSpacing {
    val xs = 4
    val sm = 8
    val md = 12
    val base = 16
    val lg = 20
    val xl = 24
}

object ZappNavBar {
    const val CLEARANCE_DP = 80
    const val FAB_BOTTOM_PADDING_DP = 72
}
