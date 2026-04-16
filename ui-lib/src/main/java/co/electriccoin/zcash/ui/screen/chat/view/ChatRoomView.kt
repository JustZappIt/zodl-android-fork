package co.electriccoin.zcash.ui.screen.chat.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import co.electriccoin.zcash.ui.screen.chat.model.ChatMessage
import co.electriccoin.zcash.ui.screen.chat.model.MessageStatus
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomView(
    conversationId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {
    val messages by viewModel.messages.collectAsState()
    val currentConversation by viewModel.currentConversation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val peerCount by viewModel.peerCount.collectAsState()
    val dhtHealth by viewModel.dhtHealth.collectAsState()
    val conversationPeerOnline by viewModel.conversationPeerOnline.collectAsState()

    var messageInput by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            currentConversation?.displayName ?: "Chat",
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium
                        )
                        val subtitleColor = when (connectionStatus) {
                            ChatViewModel.ConnectionStatus.CONNECTED -> {
                                when {
                                    conversationPeerOnline == true -> MaterialTheme.colorScheme.primary
                                    dhtHealth == ChatViewModel.DhtHealth.CRITICAL -> MaterialTheme.colorScheme.error
                                    peerCount > 0 -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.tertiary
                                }
                            }
                            ChatViewModel.ConnectionStatus.CONNECTING -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                        val subtitle = when (connectionStatus) {
                            ChatViewModel.ConnectionStatus.CONNECTED -> {
                                when {
                                    conversationPeerOnline == true -> "Peer online"
                                    dhtHealth == ChatViewModel.DhtHealth.CRITICAL -> "DHT unreachable"
                                    conversationPeerOnline == false -> "Peer offline - messages will queue"
                                    peerCount > 0 -> "P2P connected"
                                    dhtHealth == ChatViewModel.DhtHealth.DEGRADED -> "DHT degraded - no peers"
                                    else -> "Waiting for peer..."
                                }
                            }
                            ChatViewModel.ConnectionStatus.CONNECTING -> "Connecting..."
                            ChatViewModel.ConnectionStatus.DISCONNECTED -> "Offline"
                            ChatViewModel.ConnectionStatus.ERROR -> "Connection error"
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Message list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isLoading && messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                items(
                    items = messages,
                    key = { it.id }
                ) { message ->
                    MessageBubble(message = message)
                }
            }

            // Input bar
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 36.dp),
                    placeholder = { Text("Message") },
                    maxLines = 4,
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = {
                        val text = messageInput.text.trim()
                        if (text.isNotEmpty()) {
                            viewModel.sendMessage(conversationId, text)
                            messageInput = TextFieldValue("")
                        }
                    },
                    enabled = messageInput.text.isNotBlank(),
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = "Send",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isFromMe = message.isFromMe

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        if (!isFromMe && message.senderName != null) {
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            ),
            color = if (isFromMe) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFromMe) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatMessageTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFromMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isFromMe) {
                        DeliveryIndicator(
                            status = message.status,
                            tintColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryIndicator(status: MessageStatus?, tintColor: Color) {
    when (status) {
        MessageStatus.SENT -> Icon(
            Icons.Default.Done,
            contentDescription = "Sent",
            modifier = Modifier.size(13.dp),
            tint = tintColor
        )
        MessageStatus.QUEUED -> Icon(
            Icons.Default.Schedule,
            contentDescription = "Queued",
            modifier = Modifier.size(13.dp),
            tint = tintColor
        )
        MessageStatus.FAILED -> Icon(
            Icons.Default.ErrorOutline,
            contentDescription = "Failed",
            modifier = Modifier.size(13.dp),
            tint = MaterialTheme.colorScheme.error
        )
        MessageStatus.SENDING -> Icon(
            Icons.Default.MoreHoriz,
            contentDescription = "Sending",
            modifier = Modifier.size(13.dp),
            tint = tintColor
        )
        null -> {}
    }
}

private fun formatMessageTime(epochMillis: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(epochMillis))
}
