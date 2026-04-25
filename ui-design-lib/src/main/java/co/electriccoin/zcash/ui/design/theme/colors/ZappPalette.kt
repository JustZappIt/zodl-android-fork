package co.electriccoin.zcash.ui.design.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ZappColors(
    val bg: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val surfaceInput: Color,
    val border: Color,
    val borderStrong: Color,
    val text: Color,
    val textMuted: Color,
    val textSubtle: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentText: Color,
    val success: Color,
    val successSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val chipBg: Color,
    val overlay: Color,
    val navPill: Color,
    val onAccent: Color,
    val shadow: Color,
)

val LightZappColors =
    ZappColors(
        bg = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        surfaceAlt = Color(0xFFF4F2EE),
        surfaceInput = Color(0xFFF6F4F0),
        border = Color(0xFFEBE7E0),
        borderStrong = Color(0xFFD9D4CA),
        text = Color(0xFF15120D),
        textMuted = Color(0xFF6B645A),
        textSubtle = Color(0xFF9A9288),
        accent = Color(0xFFFF9417),
        accentSoft = Color(0xFFFFE7CC),
        accentText = Color(0xFFA65500),
        success = Color(0xFF2F9D6A),
        successSoft = Color(0xFFD7F0E3),
        danger = Color(0xFFD94545),
        dangerSoft = Color(0xFFFDE2E0),
        chipBg = Color(0xFFEFECE5),
        overlay = Color(0x73141210),
        navPill = Color(0xFFECEBE5),
        onAccent = Color(0xFFFFFFFF),
        shadow = Color(0x14141210),
    )

val DarkZappColors =
    ZappColors(
        bg = Color(0xFF0F0E0C),
        surface = Color(0xFF171512),
        surfaceAlt = Color(0xFF1B1916),
        surfaceInput = Color(0xFF201D19),
        border = Color(0xFF2A2622),
        borderStrong = Color(0xFF3A342D),
        text = Color(0xFFF6F2EA),
        textMuted = Color(0xFFA59C90),
        textSubtle = Color(0xFF726A60),
        accent = Color(0xFFFF9417),
        accentSoft = Color(0xFF3A2713),
        accentText = Color(0xFFFFB26B),
        success = Color(0xFF5FD49C),
        successSoft = Color(0xFF1A2E24),
        danger = Color(0xFFEF6A5F),
        dangerSoft = Color(0xFF2E1A18),
        chipBg = Color(0xFF1F1C18),
        overlay = Color(0x8C000000),
        navPill = Color(0xFF1C1A16),
        onAccent = Color(0xFF1A140B),
        shadow = Color(0x80000000),
    )

internal val LocalZappColors = staticCompositionLocalOf { LightZappColors }

internal val LocalZappDarkMode = compositionLocalOf { false }

/**
 * Static shim retained for callers that pre-date [ZappTheme]. New code should
 * read colors via `ZappTheme.colors` so dark mode resolves from the current
 * composition instead of hard-coding the light variant.
 */
object ZappPalette {
    val Primary = LightZappColors.accent
    val Accent = Color(0xFFFFB866)
    val AccentSoft = LightZappColors.accentSoft
    val AccentText = LightZappColors.accentText

    val Background = LightZappColors.bg
    val BackgroundDark = DarkZappColors.bg
    val Surface = LightZappColors.surface
    val SurfaceAlt = LightZappColors.surfaceAlt
    val SurfaceInput = LightZappColors.surfaceInput
    val SecondaryBackground = LightZappColors.surfaceAlt
    val SecondaryBackgroundDark = DarkZappColors.surfaceAlt

    val Border = LightZappColors.border
    val BorderStrong = LightZappColors.borderStrong
    val BorderDark = DarkZappColors.border

    val TextPrimary = LightZappColors.text
    val TextPrimaryDark = DarkZappColors.text
    val TextSecondary = LightZappColors.textMuted
    val TextSecondaryDark = DarkZappColors.textMuted
    val TextTertiary = LightZappColors.textSubtle
    val TextTertiaryDark = DarkZappColors.textSubtle

    val Error = LightZappColors.danger
    val ErrorSoft = LightZappColors.dangerSoft
    val Success = LightZappColors.success
    val SuccessSoft = LightZappColors.successSoft
    val Warning = LightZappColors.accent

    val OnPrimary = LightZappColors.onAccent
    val ChipBg = LightZappColors.chipBg
    val NavPill = LightZappColors.navPill
    val CardShadow = LightZappColors.shadow
}

object ZappNavBar {
    /** Bottom clearance screens must preserve for the floating pill nav. */
    const val CLEARANCE_DP = 88
    const val FAB_BOTTOM_PADDING_DP = 80
}
