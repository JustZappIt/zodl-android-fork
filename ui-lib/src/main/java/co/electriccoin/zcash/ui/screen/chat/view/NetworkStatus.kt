package co.electriccoin.zcash.ui.screen.chat.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import co.electriccoin.zcash.ui.screen.chat.model.ConnectionDetailsUi
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel

private val OkColor = Color(0xFF2E7D32)
private val WarnColor = Color(0xFFE65100)

@Composable
fun ConnectionPill(
    connectionStatus: ChatViewModel.ConnectionStatus,
    peerCount: Int,
    dhtHealth: ChatViewModel.DhtHealth,
    onClick: (() -> Unit)? = null
) {
    val errorColor = MaterialTheme.colorScheme.error
    val statusColor = when (connectionStatus) {
        ChatViewModel.ConnectionStatus.CONNECTED -> when {
            dhtHealth == ChatViewModel.DhtHealth.CRITICAL -> errorColor
            peerCount > 0 -> OkColor
            dhtHealth == ChatViewModel.DhtHealth.DEGRADED -> WarnColor
            else -> OkColor
        }
        ChatViewModel.ConnectionStatus.CONNECTING -> WarnColor
        else -> errorColor
    }
    val label = when (connectionStatus) {
        ChatViewModel.ConnectionStatus.CONNECTED -> when {
            dhtHealth == ChatViewModel.DhtHealth.CRITICAL -> "DHT unreachable"
            peerCount > 0 -> if (peerCount == 1) "1 peer" else "$peerCount peers"
            dhtHealth == ChatViewModel.DhtHealth.DEGRADED -> "DHT degraded"
            else -> "Online"
        }
        ChatViewModel.ConnectionStatus.CONNECTING -> "Connecting"
        ChatViewModel.ConnectionStatus.DISCONNECTED -> "Offline"
        ChatViewModel.ConnectionStatus.ERROR -> "Error"
    }

    Surface(
        shape = RoundedCornerShape(0.dp),
        color = statusColor.copy(alpha = 0.12f),
        modifier = Modifier
            .height(26.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                modifier = Modifier.size(7.dp),
                shape = CircleShape,
                color = statusColor
            ) {}
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkDetailsSheet(
    connectionStatus: ChatViewModel.ConnectionStatus,
    peerCount: Int,
    dhtHealth: ChatViewModel.DhtHealth,
    connectionDetails: ConnectionDetailsUi?,
    onDismiss: () -> Unit
) {
    val errorColor = MaterialTheme.colorScheme.error
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Network",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            NetworkDetailRow(
                icon = Icons.Default.Wifi,
                label = "Connection",
                value = when (connectionStatus) {
                    ChatViewModel.ConnectionStatus.CONNECTED -> "Connected"
                    ChatViewModel.ConnectionStatus.CONNECTING -> "Connecting"
                    ChatViewModel.ConnectionStatus.DISCONNECTED -> "Disconnected"
                    ChatViewModel.ConnectionStatus.ERROR -> "Error"
                },
                valueColor = when (connectionStatus) {
                    ChatViewModel.ConnectionStatus.CONNECTED -> OkColor
                    ChatViewModel.ConnectionStatus.CONNECTING -> WarnColor
                    else -> errorColor
                }
            )

            NetworkDetailRow(
                icon = Icons.Default.Hub,
                label = "DHT",
                value = when (dhtHealth) {
                    ChatViewModel.DhtHealth.HEALTHY -> "Healthy"
                    ChatViewModel.DhtHealth.DEGRADED -> "Degraded"
                    ChatViewModel.DhtHealth.CRITICAL -> "Critical"
                },
                valueColor = when (dhtHealth) {
                    ChatViewModel.DhtHealth.HEALTHY -> OkColor
                    ChatViewModel.DhtHealth.DEGRADED -> WarnColor
                    ChatViewModel.DhtHealth.CRITICAL -> errorColor
                }
            )

            NetworkDetailRow(
                icon = Icons.Default.People,
                label = "Peers",
                value = peerCount.toString(),
                valueColor = if (peerCount > 0) OkColor else secondaryColor
            )

            connectionDetails?.let { details ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                NetworkDetailRow(
                    icon = Icons.Default.Cable,
                    label = "TCP connections",
                    value = details.globalConnections.toString()
                )

                NetworkDetailRow(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    label = "Conversations",
                    value = "${details.directConversations} direct · ${details.groupConversations} group"
                )

                NetworkDetailRow(
                    icon = Icons.Default.Schedule,
                    label = "Pending",
                    value = if (details.pendingMessageCount > 0)
                        "${details.pendingMessageCount} msgs in ${details.pendingQueues} queues"
                    else "None",
                    valueColor = if (details.pendingMessageCount > 0) WarnColor else OkColor
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                NetworkDetailRow(
                    icon = Icons.Default.Security,
                    label = "Protocol",
                    value = "Zapp Messaging P2P"
                )

                NetworkDetailRow(
                    icon = Icons.Default.Lock,
                    label = "Encryption",
                    value = "End-to-end (Noise XX)"
                )
            }
        }
    }
}

@Composable
private fun NetworkDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    val effectiveValueColor = if (valueColor == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurface
    } else valueColor

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = effectiveValueColor
        )
    }
}
