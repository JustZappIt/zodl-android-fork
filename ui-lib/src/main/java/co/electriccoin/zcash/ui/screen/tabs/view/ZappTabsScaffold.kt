package co.electriccoin.zcash.ui.screen.tabs.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.electriccoin.zcash.ui.NavigationRouter
import co.electriccoin.zcash.ui.design.theme.ProvideZappTheme
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.screen.chat.ChatRoomArgs
import co.electriccoin.zcash.ui.screen.chat.ContactEditArgs
import co.electriccoin.zcash.ui.screen.chat.NewConversationArgs
import co.electriccoin.zcash.ui.screen.chat.view.ChatContactsView
import co.electriccoin.zcash.ui.screen.chat.view.ChatIdentitySetupView
import co.electriccoin.zcash.ui.screen.chat.view.ChatListView
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ZappTabsScaffold(
    navigationRouter: NavigationRouter,
) {
    ProvideZappTheme {
        ZappTabsScaffoldContent(navigationRouter = navigationRouter)
    }
}

@Composable
private fun ZappTabsScaffoldContent(
    navigationRouter: NavigationRouter,
) {
    var currentTab by rememberSaveable { mutableStateOf(ZappTab.WALLET) }

    val chatViewModel: ChatViewModel = koinViewModel()
    val unreadCount by chatViewModel.totalUnreadCount.collectAsState()
    val c = ZappTheme.colors

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        when (currentTab) {
            ZappTab.WALLET -> WalletTabContent(navigationRouter = navigationRouter)
            ZappTab.CHATS -> ChatsTabContent(
                chatViewModel = chatViewModel,
                onOpenConversation = { conversationId ->
                    navigationRouter.forward(ChatRoomArgs(conversationId))
                },
                onNewMessage = {
                    navigationRouter.forward(NewConversationArgs)
                },
                onNavigateToContacts = { currentTab = ZappTab.CONTACTS },
                onNavigateToSettings = { currentTab = ZappTab.SETTINGS }
            )
            ZappTab.CONTACTS -> ContactsTabContent(
                chatViewModel = chatViewModel,
                onOpenConversation = { conversationId ->
                    navigationRouter.forward(ChatRoomArgs(conversationId))
                },
                onEditContact = { publicKey ->
                    navigationRouter.forward(ContactEditArgs(publicKey))
                }
            )
            ZappTab.SETTINGS -> SettingsTabContent(
                navigationRouter = navigationRouter,
                chatViewModel = chatViewModel
            )
        }

        FloatingPillNavBar(
            currentTab = currentTab,
            chatUnreadCount = unreadCount,
            onTabSelected = { currentTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ChatsTabContent(
    chatViewModel: ChatViewModel,
    onOpenConversation: (String) -> Unit,
    onNewMessage: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val isInitializing by chatViewModel.isInitializing.collectAsState()
    val identity by chatViewModel.identity.collectAsState()

    val c = ZappTheme.colors
    when {
        isInitializing -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.accent)
            }
        }
        identity == null -> {
            ChatIdentitySetupView(
                onSetupComplete = { /* state will recompose */ },
                viewModel = chatViewModel,
            )
        }
        else -> {
            ChatListView(
                onConversationClick = { conversation ->
                    chatViewModel.setCurrentConversation(conversation)
                    onOpenConversation(conversation.id)
                },
                onNewMessage = onNewMessage,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToContacts = onNavigateToContacts,
                onNavigateBack = {},
                showBackButton = false,
                viewModel = chatViewModel,
            )
        }
    }
}

@Composable
private fun ContactsTabContent(
    chatViewModel: ChatViewModel,
    onOpenConversation: (String) -> Unit,
    onEditContact: (String) -> Unit
) {
    val isInitializing by chatViewModel.isInitializing.collectAsState()
    val identity by chatViewModel.identity.collectAsState()

    val c = ZappTheme.colors
    when {
        isInitializing -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.accent)
            }
        }
        identity == null -> {
            ChatIdentitySetupView(
                onSetupComplete = { /* state will recompose */ },
                viewModel = chatViewModel,
            )
        }
        else -> {
            ChatContactsView(
                onStartChat = { publicKey ->
                    chatViewModel.createDirectChat(publicKey) { conversationId ->
                        onOpenConversation(conversationId)
                    }
                },
                onNavigateBack = {},
                showBackButton = false,
                viewModel = chatViewModel
            )
        }
    }
}
