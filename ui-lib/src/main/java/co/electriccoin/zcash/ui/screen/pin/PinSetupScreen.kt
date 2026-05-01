package co.electriccoin.zcash.ui.screen.pin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.electriccoin.zcash.ui.screen.pin.view.PinScreen
import org.koin.androidx.compose.koinViewModel

/**
 * Two-phase create/confirm flow used during onboarding (and any time the user
 * resets their PIN). Persists the hash through [PinSetupVM] and signals the
 * caller via [onComplete] once the confirm phase succeeds.
 */
@Composable
fun PinSetupScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit,
) {
    val vm: PinSetupVM = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is PinSetupEvent.Completed -> onComplete()
            }
        }
    }

    val isCreate = !state.isConfirmPhase
    PinScreen(
        title = if (isCreate) "Create a PIN" else "Confirm PIN",
        subtitle = if (isCreate) "Used to unlock Zapp and confirm payments." else "Enter the same PIN again.",
        value = state.activeBuffer,
        onDigit = vm::onDigit,
        onBackspace = vm::onBackspace,
        onBack = if (isCreate) onBack else vm::onBack,
        backLabel = "Back",
        errorText = state.error,
        enabled = !state.isBusy,
    )
}
