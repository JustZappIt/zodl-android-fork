package co.electriccoin.zcash.ui.screen.offramp

import co.electriccoin.zcash.ui.design.component.ModalBottomSheetState
import co.electriccoin.zcash.ui.design.util.StringResource

data class OfframpState(
    override val onBack: () -> Unit,
    val amount: String,
    val fiatAmount: StringResource?,
    val onAmountChange: (String) -> Unit,
    val onContinue: () -> Unit,
    val isContinueEnabled: Boolean,
    val attribution: StringResource,
) : ModalBottomSheetState
