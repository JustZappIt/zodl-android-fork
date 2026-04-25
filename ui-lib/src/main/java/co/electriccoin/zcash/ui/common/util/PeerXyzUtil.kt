package co.electriccoin.zcash.ui.common.util

import java.util.Locale

object PeerXyzUtil {
    private val UNSUPPORTED_REGIONS = setOf("BR", "IN", "PH", "KE", "TZ", "UG", "GH")
    const val JUSTZAPPIT_URL = "https://justzappit.xyz/directory"

    fun isPeerAvailable(): Boolean = Locale.getDefault().country !in UNSUPPORTED_REGIONS

    fun buildOnrampUrl(unifiedAddress: String): String =
        "https://www.peer.xyz/?recipientAddress=$unifiedAddress&toToken=ZEC&referrer=Zapp"

    fun getOnrampUrl(unifiedAddress: String): String =
        if (isPeerAvailable()) buildOnrampUrl(unifiedAddress) else JUSTZAPPIT_URL
}
