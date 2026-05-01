package co.electriccoin.zcash.ui.screen.chat.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.electriccoin.zcash.ui.design.component.zapp.ZappBackButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappChipVariant
import co.electriccoin.zcash.ui.design.component.zapp.ZappFab
import co.electriccoin.zcash.ui.design.component.zapp.ZappRowDivider
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.component.zapp.ZappStatusChip
import co.electriccoin.zcash.ui.design.component.zapp.initialsOf
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.design.theme.colors.ZappNavBar
import co.electriccoin.zcash.ui.screen.chat.model.ChatConversation
import co.electriccoin.zcash.ui.screen.chat.model.ConversationType
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatListView(
    onConversationClick: (ChatConversation) -> Unit,
    onNewMessage: () -> Unit,
    onNavigateBack: () -> Unit = {},
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
) {
    val c = ZappTheme.colors
    val conversations by viewModel.conversations.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val peerCount by viewModel.peerCount.collectAsState()
    val dhtHealth by viewModel.dhtHealth.collectAsState()
    val connectionDetails by viewModel.connectionDetails.collectAsState()
    var showNetworkSheet by remember { mutableStateOf(false) }

    val sortedConversations =
        remember(conversations) {
            conversations.sortedByDescending { it.lastMessageTimestamp ?: 0L }
        }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ZappScreenHeader(
                title = "Chats",
                right = {
                    NetworkChip(
                        connectionStatus = connectionStatus,
                        peerCount = peerCount,
                        onClick = {
                            viewModel.fetchConnectionDetails()
                            showNetworkSheet = true
                        },
                    )
                },
            )

            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = c.textSubtle,
                        )
                        Spacer(Modifier.height(12.dp))
                        BasicText(
                            "No conversations yet",
                            style = ZappTheme.typography.sectionTitle.copy(color = c.text),
                        )
                        Spacer(Modifier.height(6.dp))
                        BasicText(
                            "Tap + to start a new P2P chat",
                            style = ZappTheme.typography.body.copy(color = c.textMuted),
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 4.dp,
                        bottom = ZappNavBar.CLEARANCE_DP.dp,
                    ),
                ) {
                    items(
                        items = sortedConversations,
                        key = { it.id },
                    ) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            onClick = { onConversationClick(conversation) },
                        )
                        ZappRowDivider(inset = true)
                    }
                }
            }
        }

        ZappFab(
            icon = Icons.Default.Add,
            contentDescription = "New conversation",
            onClick = onNewMessage,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 20.dp,
                    bottom = (ZappNavBar.CLEARANCE_DP + 12).dp,
                ),
        )

        // Back button floats bottom-left, horizontally aligned with the FAB.
        if (showBackButton) {
            ZappBackButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 20.dp,
                        bottom = (ZappNavBar.CLEARANCE_DP + 12).dp,
                    ),
            )
        }
    }

    if (showNetworkSheet) {
        NetworkDetailsSheet(
            connectionStatus = connectionStatus,
            peerCount = peerCount,
            dhtHealth = dhtHealth,
            connectionDetails = connectionDetails,
            onDismiss = { showNetworkSheet = false },
        )
    }
}

@Composable
private fun NetworkChip(
    connectionStatus: ChatViewModel.ConnectionStatus,
    peerCount: Int,
    onClick: () -> Unit,
) {
    val c = ZappTheme.colors
    val (variant, dotColor, text) =
        when (connectionStatus) {
            ChatViewModel.ConnectionStatus.CONNECTED ->
                Triple(ZappChipVariant.Success, c.success, "$peerCount")
            ChatViewModel.ConnectionStatus.CONNECTING ->
                Triple(ZappChipVariant.Accent, c.accent, "...")
            ChatViewModel.ConnectionStatus.DISCONNECTED ->
                Triple(ZappChipVariant.Danger, c.danger, "off")
            ChatViewModel.ConnectionStatus.ERROR ->
                Triple(ZappChipVariant.Danger, c.danger, "err")
        }
    ZappStatusChip(
        text = text,
        variant = variant,
        dotColor = dotColor,
        onClick = onClick,
    )
}

@Composable
private fun ConversationItem(
    conversation: ChatConversation,
    onClick: () -> Unit,
) {
    val c = ZappTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ConversationAvatar(
            name = conversation.displayName,
            group = conversation.type == ConversationType.GROUP,
        )

        Spacer(Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = conversation.displayName,
                    style = ZappTheme.typography.rowTitle.copy(color = c.text),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                conversation.lastMessageTimestamp?.let { ts ->
                    BasicText(
                        formatRelativeTime(ts),
                        style = ZappTheme.typography.caption.copy(color = c.textSubtle),
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = conversation.lastMessage ?: "No messages yet",
                    style = ZappTheme.typography.rowSubtitle.copy(color = c.textMuted),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (conversation.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(c.accent, RectangleShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        BasicText(
                            text = "${conversation.unreadCount}",
                            style = ZappTheme.typography.chip.copy(color = c.onAccent),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationAvatar(
    name: String,
    group: Boolean,
) {
    val c = ZappTheme.colors
    val initials = remember(name) { initialsOf(name) }
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(c.accent, RectangleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (group || initials.isBlank()) {
            Icon(
                imageVector = if (group) Icons.Default.Group else Icons.Default.Person,
                contentDescription = null,
                tint = c.onAccent,
                modifier = Modifier.size(20.dp),
            )
        } else {
            BasicText(
                text = initials,
                style = ZappTheme.typography.rowTitle.copy(color = c.onAccent),
            )
        }
    }
}

private fun formatRelativeTime(epochMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - epochMillis
    return when {
        diff < 60_000 -> "now"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(epochMillis))
        diff < 604_800_000 -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(epochMillis))
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochMillis))
    }
}
