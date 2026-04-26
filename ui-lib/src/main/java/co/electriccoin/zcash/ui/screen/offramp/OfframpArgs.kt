package co.electriccoin.zcash.ui.screen.offramp

import kotlinx.serialization.Serializable

@Serializable
data class OfframpArgs(
    val prefillZatoshi: Long? = null
)
