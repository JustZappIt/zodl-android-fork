package co.electriccoin.zcash.ui.common.util

import java.util.Locale

object PeerXyzUtil {
    // Remove a country code once its ZKP2P provider is live on peer.xyz (see zkp2p-providers/).
    private val UNSUPPORTED_REGIONS = setOf("BR", "IN", "PH", "KE", "TZ", "UG", "GH")
    const val JUSTZAPPIT_URL = "https://justzappit.xyz/directory"

    fun isPeerAvailable(): Boolean = Locale.getDefault().country !in UNSUPPORTED_REGIONS

    fun getOnrampUrl(unifiedAddress: String): String =
        if (isPeerAvailable()) {
            "https://www.peer.xyz/?recipientAddress=$unifiedAddress&toToken=ZEC&referrer=Zapp"
        } else {
            JUSTZAPPIT_URL
        }

    fun getOfframpUrl(zecAmount: String): String =
        if (isPeerAvailable()) {
            "https://www.peer.xyz/sell?amount=$zecAmount&referrer=Zapp"
        } else {
            JUSTZAPPIT_URL
        }
}
