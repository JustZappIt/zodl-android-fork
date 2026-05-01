package co.electriccoin.zcash.ui.common.repository

import android.os.SystemClock
import co.electriccoin.zcash.preference.EncryptedPreferenceProvider
import co.electriccoin.zcash.preference.api.PreferenceProvider
import co.electriccoin.zcash.preference.model.entry.PreferenceKey
import co.electriccoin.zcash.spackle.Twig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

interface PinRepository {
    val state: StateFlow<PinState>

    suspend fun isPinSet(): Boolean

    suspend fun setPin(pin: String)

    suspend fun verifyPin(pin: String): VerifyResult

    suspend fun clearPin()

    suspend fun refresh()
}

data class PinState(
    val failedAttempts: Int = 0,
    val lockoutRemainingMs: Long = 0L,
) {
    val isLockedOut: Boolean get() = lockoutRemainingMs > 0L
}

sealed interface VerifyResult {
    data object Success : VerifyResult
    data class Failure(val attemptsBeforeNextLockout: Int) : VerifyResult
    data class LockedOut(val lockoutRemainingMs: Long) : VerifyResult
}

/**
 * App-lock PIN persistence.
 *
 * Lockout is tracked against [SystemClock.elapsedRealtime] (monotonic since boot)
 * so the user can't bypass the timer by editing wallclock in Settings. We persist
 * both the elapsed-realtime target and the elapsed-realtime at the moment we
 * persisted; if the latter is in the future relative to "now," the device has
 * rebooted and the lockout is treated as cleared. Failed-attempt count survives
 * reboot via encrypted prefs.
 */
@OptIn(ExperimentalEncodingApi::class)
class PinRepositoryImpl(
    private val encryptedPreferenceProvider: EncryptedPreferenceProvider,
) : PinRepository {
    private val mutableState = MutableStateFlow(PinState())
    override val state: StateFlow<PinState> = mutableState.asStateFlow()

    override suspend fun isPinSet(): Boolean {
        val prefs = encryptedPreferenceProvider()
        return prefs.getString(KEY_HASH) != null && prefs.getString(KEY_SALT) != null
    }

    override suspend fun setPin(pin: String) = withContext(Dispatchers.Default) {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash(pin, salt)
        val prefs = encryptedPreferenceProvider()
        prefs.putString(KEY_SALT, Base64.encode(salt))
        prefs.putString(KEY_HASH, Base64.encode(hash))
        prefs.putString(KEY_FAILED_ATTEMPTS, "0")
        prefs.remove(KEY_LOCKOUT_UNTIL_ELAPSED)
        prefs.remove(KEY_LOCKOUT_PERSISTED_AT)
        mutableState.value = PinState()
    }

    override suspend fun verifyPin(pin: String): VerifyResult = withContext(Dispatchers.Default) {
        val prefs = encryptedPreferenceProvider()
        val nowElapsed = SystemClock.elapsedRealtime()
        val current = readState(prefs, nowElapsed)
        if (current.isLockedOut) {
            mutableState.value = current
            return@withContext VerifyResult.LockedOut(current.lockoutRemainingMs)
        }

        val salt = prefs.getString(KEY_SALT)?.let { Base64.decode(it) }
        val hash = prefs.getString(KEY_HASH)?.let { Base64.decode(it) }
        if (salt == null || hash == null) {
            // No PIN registered but caller still tried to verify — likely a state bug
            // upstream (APP_LOCK_MODE=PIN with no hash). Don't silently succeed.
            Twig.error { "PinRepository.verifyPin called with no PIN registered" }
            return@withContext VerifyResult.Failure(attemptsBeforeNextLockout = ATTEMPTS_BEFORE_LOCKOUT)
        }

        if (PinHasher.verify(pin, salt, hash)) {
            persistState(prefs, attempts = 0, lockoutDurationMs = 0L, nowElapsed = nowElapsed)
            VerifyResult.Success
        } else {
            val attempts = current.failedAttempts + 1
            val lockoutMs = lockoutDurationMs(attempts)
            persistState(prefs, attempts = attempts, lockoutDurationMs = lockoutMs ?: 0L, nowElapsed = nowElapsed)
            if (lockoutMs != null) {
                VerifyResult.LockedOut(lockoutMs)
            } else {
                VerifyResult.Failure(
                    attemptsBeforeNextLockout = ATTEMPTS_BEFORE_LOCKOUT - (attempts % ATTEMPTS_BEFORE_LOCKOUT),
                )
            }
        }
    }

    override suspend fun clearPin() {
        val prefs = encryptedPreferenceProvider()
        prefs.remove(KEY_SALT)
        prefs.remove(KEY_HASH)
        prefs.remove(KEY_FAILED_ATTEMPTS)
        prefs.remove(KEY_LOCKOUT_UNTIL_ELAPSED)
        prefs.remove(KEY_LOCKOUT_PERSISTED_AT)
        mutableState.value = PinState()
    }

    override suspend fun refresh() {
        val nowElapsed = SystemClock.elapsedRealtime()
        mutableState.value = readState(encryptedPreferenceProvider(), nowElapsed)
    }

    private suspend fun readState(prefs: PreferenceProvider, nowElapsed: Long): PinState {
        val attempts = prefs.getString(KEY_FAILED_ATTEMPTS)?.toIntOrNull() ?: 0
        val until = prefs.getString(KEY_LOCKOUT_UNTIL_ELAPSED)?.toLongOrNull() ?: 0L
        val persistedAt = prefs.getString(KEY_LOCKOUT_PERSISTED_AT)?.toLongOrNull() ?: 0L
        // If our previously-persisted "now" is in the future relative to current
        // elapsedRealtime, the device has rebooted — elapsedRealtime resets to 0
        // on boot, so the persisted target is meaningless. Treat as no lockout.
        val remaining = if (until == 0L || nowElapsed < persistedAt) {
            0L
        } else {
            (until - nowElapsed).coerceAtLeast(0L)
        }
        return PinState(failedAttempts = attempts, lockoutRemainingMs = remaining)
    }

    private suspend fun persistState(
        prefs: PreferenceProvider,
        attempts: Int,
        lockoutDurationMs: Long,
        nowElapsed: Long,
    ) {
        prefs.putString(KEY_FAILED_ATTEMPTS, attempts.toString())
        if (lockoutDurationMs > 0L) {
            prefs.putString(KEY_LOCKOUT_UNTIL_ELAPSED, (nowElapsed + lockoutDurationMs).toString())
            prefs.putString(KEY_LOCKOUT_PERSISTED_AT, nowElapsed.toString())
        } else {
            prefs.remove(KEY_LOCKOUT_UNTIL_ELAPSED)
            prefs.remove(KEY_LOCKOUT_PERSISTED_AT)
        }
        mutableState.value = PinState(failedAttempts = attempts, lockoutRemainingMs = lockoutDurationMs)
    }

    /**
     * Progressive backoff: every [ATTEMPTS_BEFORE_LOCKOUT] failures triggers a
     * lockout that escalates with the total attempt count. A successful unlock
     * resets [PinState.failedAttempts] to zero.
     */
    private fun lockoutDurationMs(totalAttempts: Int): Long? {
        if (totalAttempts == 0 || totalAttempts % ATTEMPTS_BEFORE_LOCKOUT != 0) return null
        return when (totalAttempts / ATTEMPTS_BEFORE_LOCKOUT) {
            1 -> 30_000L
            2 -> 60_000L
            3 -> 5 * 60_000L
            else -> 30 * 60_000L
        }
    }

    companion object {
        private const val ATTEMPTS_BEFORE_LOCKOUT = 5

        private val KEY_SALT = PreferenceKey("pin_salt")
        private val KEY_HASH = PreferenceKey("pin_hash")
        private val KEY_FAILED_ATTEMPTS = PreferenceKey("pin_failed_attempts")
        private val KEY_LOCKOUT_UNTIL_ELAPSED = PreferenceKey("pin_lockout_until_elapsed")
        private val KEY_LOCKOUT_PERSISTED_AT = PreferenceKey("pin_lockout_persisted_at_elapsed")
    }
}
