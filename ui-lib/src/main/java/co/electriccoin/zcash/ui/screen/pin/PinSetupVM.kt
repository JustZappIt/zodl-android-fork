package co.electriccoin.zcash.ui.screen.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.electriccoin.zcash.ui.common.repository.PinRepository
import co.electriccoin.zcash.ui.screen.pin.view.PIN_LENGTH
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PinSetupVM(
    private val pinRepository: PinRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(PinSetupState())
    val state: StateFlow<PinSetupState> = mutableState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<PinSetupEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PinSetupEvent> = mutableEvents.asSharedFlow()

    fun onDigit(digit: Char) {
        mutableState.update { current ->
            if (current.isBusy || current.activeBuffer.length >= PIN_LENGTH) return@update current
            current.appendToActive(digit)
        }
        if (mutableState.value.activeBuffer.length == PIN_LENGTH) {
            advance()
        }
    }

    fun onBackspace() {
        mutableState.update { current ->
            if (current.isBusy || current.activeBuffer.isEmpty()) return@update current
            current.dropLastFromActive()
        }
    }

    fun onBack() {
        mutableState.update { current ->
            if (current.isConfirmPhase) {
                current.copy(isConfirmPhase = false, firstPin = "", confirmPin = "", error = null)
            } else {
                current
            }
        }
    }

    private fun advance() {
        viewModelScope.launch {
            val snapshot = mutableState.value
            if (!snapshot.isConfirmPhase) {
                // Move to confirm with a short pause so the dots animation is visible.
                delay(PHASE_TRANSITION_DELAY_MS)
                mutableState.update { it.copy(isConfirmPhase = true, error = null) }
                return@launch
            }
            mutableState.update { it.copy(isBusy = true) }
            if (snapshot.firstPin == snapshot.confirmPin) {
                pinRepository.setPin(snapshot.confirmPin)
                // Drop plaintext PIN from VM state once it's persisted.
                mutableState.update { it.copy(firstPin = "", confirmPin = "") }
                mutableEvents.tryEmit(PinSetupEvent.Completed)
            } else {
                delay(MISMATCH_FEEDBACK_DELAY_MS)
                mutableState.update {
                    it.copy(
                        confirmPin = "",
                        error = "PINs don't match. Try again.",
                        isBusy = false,
                    )
                }
            }
        }
    }

    companion object {
        private const val PHASE_TRANSITION_DELAY_MS = 180L
        private const val MISMATCH_FEEDBACK_DELAY_MS = 220L
    }
}

data class PinSetupState(
    val isConfirmPhase: Boolean = false,
    val firstPin: String = "",
    val confirmPin: String = "",
    val error: String? = null,
    val isBusy: Boolean = false,
) {
    val activeBuffer: String
        get() = if (isConfirmPhase) confirmPin else firstPin

    fun appendToActive(digit: Char): PinSetupState =
        if (isConfirmPhase) copy(confirmPin = confirmPin + digit, error = null)
        else copy(firstPin = firstPin + digit, error = null)

    fun dropLastFromActive(): PinSetupState =
        if (isConfirmPhase) copy(confirmPin = confirmPin.dropLast(1))
        else copy(firstPin = firstPin.dropLast(1))
}

sealed interface PinSetupEvent {
    data object Completed : PinSetupEvent
}
