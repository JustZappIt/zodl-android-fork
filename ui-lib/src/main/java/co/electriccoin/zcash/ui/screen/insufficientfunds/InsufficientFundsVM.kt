package co.electriccoin.zcash.ui.screen.insufficientfunds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.electriccoin.zcash.ui.NavigationRouter
import co.electriccoin.zcash.ui.common.usecase.NavigateToPeerOnrampUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InsufficientFundsVM(
    private val navigationRouter: NavigationRouter,
    private val navigateToPeerOnramp: NavigateToPeerOnrampUseCase,
) : ViewModel() {
    val state: StateFlow<InsufficientFundsState?> =
        MutableStateFlow<InsufficientFundsState?>(
            InsufficientFundsState(
                onBack = { navigationRouter.back() },
                onAddZec = { viewModelScope.launch { navigateToPeerOnramp() } },
            )
        ).asStateFlow()
}
