package co.electriccoin.zcash.ui.common.repository

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PBKDF2 helper for the app-lock PIN. The 6-digit space is tiny (10^6), so we
 * lean on iteration count + a per-PIN salt to make offline brute-force costly
 * if encrypted prefs ever leak. Online attempts are throttled separately by
 * [PinRepository]'s lockout policy.
 */
internal object PinHasher {
    private const val ITERATIONS = 200_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun newSalt(): ByteArray {
        val bytes = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    fun hash(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    fun verify(pin: String, salt: ByteArray, expectedHash: ByteArray): Boolean =
        MessageDigest.isEqual(hash(pin, salt), expectedHash)
}
