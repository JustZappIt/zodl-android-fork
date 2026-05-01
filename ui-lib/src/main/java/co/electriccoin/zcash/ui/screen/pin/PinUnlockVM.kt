package co.electriccoin.zcash.ui.screen.pin

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.electriccoin.zcash.ui.common.repository.PinRepository
import co.electriccoin.zcash.ui.common.repository.VerifyResult
import co.electriccoin.zcash.ui.screen.pin.view.PIN_LENGTH
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PinUnlockVM(
    private val pinRepository: PinRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(PinUnlockState())
    val state: StateFlow<PinUnlockState> = mutableState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<PinUnlockEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PinUnlockEvent> = mutableEvents.asSharedFlow()

    private var lockoutTickerJob: Job? = null

    init {
        viewModelScope.launch {
            pinRepository.refresh()
            val initial = pinRepository.state.value
            if (initial.isLockedOut) {
                startLockoutCountdown(initial.lockoutRemainingMs)
            }
        }
    }

    fun onDigit(digit: Char) {
        mutableState.update { current ->
            if (current.isBusy || current.lockoutRemainingMs > 0L) return@update current
            if (current.value.length >= PIN_LENGTH) return@update current
            current.copy(value = current.value + digit, error = null)
        }
        if (mutableState.value.value.length == PIN_LENGTH) {
            verify()
        }
    }

    fun onBackspace() {
        mutableState.update { current ->
            if (current.isBusy || current.value.isEmpty()) current
            else current.copy(value = current.value.dropLast(1), error = null)
        }
    }

    private fun verify() {
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true) }
            when (val result = pinRepository.verifyPin(mutableState.value.value)) {
                VerifyResult.Success -> {
                    mutableState.update { it.copy(value = "", isBusy = false, error = null) }
                    mutableEvents.tryEmit(PinUnlockEvent.Success)
                }
                is VerifyResult.Failure -> {
                    delay(220)
                    mutableState.update {
                        it.copy(
                            value = "",
                            isBusy = false,
                            error = "Wrong PIN. ${result.attemptsBeforeNextLockout} attempts left.",
                        )
                    }
                }
                is VerifyResult.LockedOut -> {
                    mutableState.update { it.copy(value = "", isBusy = false, error = null) }
                    startLockoutCountdown(result.lockoutRemainingMs)
                }
            }
        }
    }

    private fun startLockoutCountdown(initialRemainingMs: Long) {
        lockoutTickerJob?.cancel()
        lockoutTickerJob = viewModelScope.launch {
            // Anchor against elapsedRealtime so wallclock changes can't speed up
            // the countdown. The repository persists against elapsedRealtime too.
            val targetElapsed = SystemClock.elapsedRealtime() + initialRemainingMs
            mutableState.update { it.copy(value = "", lockoutRemainingMs = initialRemainingMs) }
            while (isActive) {
                val remaining = (targetElapsed - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                mutableState.update { it.copy(lockoutRemainingMs = remaining) }
                if (remaining <= 0L) {
                    pinRepository.refresh()
                    return@launch
                }
                delay(LOCKOUT_TICK_MS)
            }
        }
    }

    companion object {
        private const val LOCKOUT_TICK_MS = 250L
    }
}

data class PinUnlockState(
    val value: String = "",
    val lockoutRemainingMs: Long = 0L,
    val error: String? = null,
    val isBusy: Boolean = false,
) {
    fun lockoutSecondsRemaining(): Int =
        if (lockoutRemainingMs <= 0L) 0 else ((lockoutRemainingMs + 999L) / 1000L).toInt()
}

sealed interface PinUnlockEvent {
    data object Success : PinUnlockEvent
}
