package co.electriccoin.zcash.ui.common.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.sdk.model.BlockHeight
import cash.z.ecc.android.sdk.model.SeedPhrase
import cash.z.ecc.android.sdk.model.ZcashNetwork
import co.electriccoin.zcash.ui.common.provider.SynchronizerProvider
import co.electriccoin.zcash.ui.common.repository.WalletRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import xyz.justzappit.zappmessaging.ZappMessagingSDK

// To make this more multiplatform compatible, we need to remove the dependency on Context
// for loading the preferences.
// TODO [#292]: Should be moved to SDK-EXT-UI module.
// TODO [#292]: https://github.com/Electric-Coin-Company/zashi-android/issues/292
class WalletViewModel(
    application: Application,
    synchronizerProvider: SynchronizerProvider,
    private val walletRepository: WalletRepository,
    private val messagingSDK: ZappMessagingSDK,
) : AndroidViewModel(application) {
    val synchronizer = synchronizerProvider.synchronizer

    val secretState: StateFlow<SecretState> = walletRepository.secretState

    fun createNewWallet() {
        walletRepository.createNewWallet()
        // Also create a P2P chat identity
        viewModelScope.launch {
            try {
                messagingSDK.initialize(getApplication())
                if (messagingSDK.identity.value == null) {
                    messagingSDK.createIdentity("Zodl User")
                    Log.i(TAG, "Chat identity created alongside new wallet")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to create chat identity: ${e.message}")
            }
        }
    }

    fun persistExistingWalletWithSeedPhrase(
        network: ZcashNetwork,
        seedPhrase: SeedPhrase,
        birthday: BlockHeight
    ) {
        walletRepository.restoreWallet(network, seedPhrase, birthday)
        // Also restore the P2P chat identity from the same seed
        viewModelScope.launch {
            try {
                messagingSDK.initialize(getApplication())
                if (messagingSDK.identity.value == null) {
                    val seedWords = seedPhrase.joinToString()
                    messagingSDK.restoreFromSeedPhrase(seedWords, "Zodl User")
                    Log.i(TAG, "Chat identity restored alongside wallet restore")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to restore chat identity: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "WalletViewModel"
    }
}

/**
 * Represents the state of the wallet secret.
 */
enum class SecretState {
    LOADING,
    NONE,
    READY
}

/**
 * This constant sets the default limitation on the length of the stack trace in the [co.electriccoin.zcash.ui.common.model.SynchronizerError]
 */
const val STACKTRACE_LIMIT = 250

// TODO [#529]: Localize Synchronizer Errors
// TODO [#529]: https://github.com/Electric-Coin-Company/zashi-android/issues/529
