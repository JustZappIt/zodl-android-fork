package co.electriccoin.zcash.ui.common.model

/**
 * How the app gates access on launch (and other auth-required moments).
 *
 * WARN: Persisted as ordinal — do NOT re-order.
 */
enum class AppLockMode {
    NONE,
    BIOMETRIC,
    PIN;

    fun toNumber() = ordinal

    companion object {
        fun fromNumber(ordinal: Int) = entries[ordinal]
    }
}
