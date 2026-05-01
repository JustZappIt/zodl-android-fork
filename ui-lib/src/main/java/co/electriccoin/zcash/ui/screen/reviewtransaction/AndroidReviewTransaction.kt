package co.electriccoin.zcash.ui.screen.reviewtransaction

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.electriccoin.zcash.ui.screen.onboarding.view.PinVerifyScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AndroidReviewTransaction() {
    val vm = koinViewModel<ReviewTransactionVM>()
    val state by vm.state.collectAsStateWithLifecycle()
    val sendAuthState by vm.sendAuthState.collectAsStateWithLifecycle()

    BackHandler {
        if (sendAuthState != ReviewTransactionVM.SendAuthState.Idle) {
            vm.onSendAuthDismissed()
        } else {
            state?.onBack?.invoke()
        }
    }

    when (sendAuthState) {
        ReviewTransactionVM.SendAuthState.PinRequired,
        ReviewTransactionVM.SendAuthState.PinError -> {
            PinVerifyScreen(
                hasError = sendAuthState == ReviewTransactionVM.SendAuthState.PinError,
                onPinSubmit = { pin -> vm.onSendPinSubmitted(pin) },
                onCancel = { vm.onSendAuthDismissed() }
            )
        }
        else -> state?.let { ReviewTransactionView(it) }
    }
}
