package co.electriccoin.zcash.ui.common.usecase

import co.electriccoin.zcash.ui.NavigationRouter
import co.electriccoin.zcash.ui.common.datasource.AccountDataSource
import co.electriccoin.zcash.ui.common.util.PeerXyzUtil
import co.electriccoin.zcash.ui.screen.ExternalUrl

class NavigateToPeerOnrampUseCase(
    private val accountDataSource: AccountDataSource,
    private val navigationRouter: NavigationRouter,
) {
    operator fun invoke() {
        val account = accountDataSource.selectedAccount.value ?: return
        val ua = account.unified.address.address
        val url = PeerXyzUtil.getOnrampUrl(ua)
        navigationRouter.forward(ExternalUrl(url = url, branded = true))
    }
}
