package co.electriccoin.zcash.ui.preference

import co.electriccoin.zcash.preference.model.entry.PreferenceKey
import co.electriccoin.zcash.preference.model.entry.StringPreferenceDefault
import java.security.MessageDigest

/**
 * Preference keys stored in [co.electriccoin.zcash.preference.EncryptedPreferenceProvider].
 * Only put sensitive data here — values are encrypted at rest.
 */
object EncryptedPreferenceKeys {
    /**
     * SHA-256 hex digest of the user's onboarding PIN. Empty string means no PIN has been set.
     */
    val APP_PIN_HASH = StringPreferenceDefault(
        key = PreferenceKey("app_pin_hash"),
        defaultValue = ""
    )

    /**
     * Hashes [pin] with SHA-256 and returns the hex digest. All PIN storage and
     * verification must use this function to ensure consistent hashing.
     */
    fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
