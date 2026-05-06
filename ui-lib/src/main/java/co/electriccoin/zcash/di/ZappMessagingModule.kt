package co.electriccoin.zcash.di

import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import xyz.justzappit.zappmessaging.ZappMessagingSDK

val zappMessagingModule = module {
    single { ZappMessagingSDK() }
    viewModel { ChatViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
}
