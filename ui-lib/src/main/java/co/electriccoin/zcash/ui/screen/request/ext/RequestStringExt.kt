package co.electriccoin.zcash.ui.screen.request.ext

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

internal fun String.toBigDecimalLocalized(locale: Locale): BigDecimal? =
    try {
        val currencyFormatter =
            (NumberFormat.getInstance(locale) as DecimalFormat).apply {
                isParseBigDecimal = true
            }
        currencyFormatter.parse(this) as BigDecimal
    } catch (_: ParseException) {
        null
    }
