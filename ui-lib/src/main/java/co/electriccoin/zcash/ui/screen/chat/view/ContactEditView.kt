package co.electriccoin.zcash.ui.screen.chat.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import co.electriccoin.zcash.ui.design.component.zapp.ZappBottomActionBar
import co.electriccoin.zcash.ui.design.component.zapp.ZappButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel

@Composable
fun ContactEditView(
    publicKey: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {
    val c = ZappTheme.colors
    val contacts by viewModel.contacts.collectAsState()
    val contact = contacts.find { it.publicKey == publicKey }

    var nameInput by remember(contact) {
        mutableStateOf(TextFieldValue(contact?.name ?: ""))
    }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ZappScreenHeader(
                title = "Edit Contact",
                right = {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = c.danger,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
            )
        },
        bottomBar = {
            ZappBottomActionBar(
                onBack = onNavigateBack,
                primaryAction = {
                    ZappButton(
                        text = "Save",
                        enabled = nameInput.text.isNotBlank(),
                        onClick = {
                            val name = nameInput.text.trim()
                            if (name.isNotEmpty()) {
                                viewModel.updateContact(publicKey, name)
                                onNavigateBack()
                            }
                        },
                    )
                },
            )
        },
        containerColor = c.bg,
        modifier = modifier,
    ) { paddingValues ->
        if (contact == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
            ) {
                BasicText(
                    "Contact not found",
                    style = ZappTheme.typography.body.copy(color = c.textMuted),
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Public key card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.surfaceAlt, RectangleShape)
                        .border(
                            androidx.compose.foundation.BorderStroke(1.dp, c.border),
                            RectangleShape,
                        )
                        .padding(16.dp),
                ) {
                    Column {
                        BasicText(
                            "Public Key",
                            style = ZappTheme.typography.caption.copy(color = c.textMuted),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BasicText(
                            publicKey,
                            style = ZappTheme.typography.mono.copy(color = c.text),
                            maxLines = 3,
                        )
                    }
                }

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Contact") },
            text = { Text("Are you sure you want to delete this contact?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteContact(publicKey)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }
}
