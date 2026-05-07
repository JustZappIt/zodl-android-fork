package co.electriccoin.zcash.ui.screen.chat.model

import xyz.justzappit.zappmessaging.ZappMessagingSDK

/**
 * UI-layer projection of [ZappMessagingSDK.ConnectionDetails] so that views and
 * Composables never need to import the SDK directly.
 */
data class ConnectionDetailsUi(
    val globalConnections: Int,
    val directConversations: Int,
    val groupConversations: Int,
    val pendingMessageCount: Int,
    val pendingQueues: Int,
) {
    companion object {
        fun from(details: ZappMessagingSDK.ConnectionDetails): ConnectionDetailsUi =
            ConnectionDetailsUi(
                globalConnections = details.globalConnections,
                directConversations = details.directConversations,
                groupConversations = details.groupConversations,
                pendingMessageCount = details.pendingMessageCount,
                pendingQueues = details.pendingQueues,
            )
    }
}
