package co.electriccoin.zcash.ui.screen.chat.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.electriccoin.zcash.ui.design.component.zapp.ZappBackButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappChipVariant
import co.electriccoin.zcash.ui.design.component.zapp.ZappFab
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.component.zapp.ZappStatusChip
import co.electriccoin.zcash.ui.design.component.zapp.ellipsizeAddress
import co.electriccoin.zcash.ui.design.component.zapp.initialsOf
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.design.theme.colors.ZappNavBar
import co.electriccoin.zcash.ui.screen.chat.model.ChatContact
import co.electriccoin.zcash.ui.screen.chat.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContactsView(
    onStartChat: (String) -> Unit,
    onNavigateBack: () -> Unit,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {
    val contacts by viewModel.contacts.collectAsState()
    val scannedPublicKey by viewModel.scannedPublicKey.collectAsState()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadContacts()
    }

    // Reopen the sheet automatically after a scan completes — the scanner
    // screen disposes this composition, so the sheet has to be re-summoned
    // once we're back.
    LaunchedEffect(scannedPublicKey) {
        if (scannedPublicKey != null) {
            showAddDialog = true
        }
    }

    val c = ZappTheme.colors
    val grouped =
        remember(contacts) {
            contacts
                .sortedBy { it.name.lowercase() }
                .groupBy { (it.name.firstOrNull() ?: '?').uppercaseChar() }
        }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ZappScreenHeader(
                title = "Contacts",
                right = {
                    ZappStatusChip(
                        text = "${contacts.size} saved",
                        variant = ZappChipVariant.Muted,
                    )
                },
            )

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp),
                    ) {
                        Icon(
                            Icons.Default.Contacts,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = c.textSubtle,
                        )
                        Spacer(Modifier.height(12.dp))
                        BasicText(
                            "No contacts yet",
                            style = ZappTheme.typography.sectionTitle.copy(color = c.text),
                        )
                        Spacer(Modifier.height(6.dp))
                        BasicText(
                            "Add contacts to start chatting",
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
                    grouped.forEach { (letter, bucket) ->
                        item(key = "header-$letter") {
                            BasicText(
                                text = letter.toString(),
                                style = ZappTheme.typography.groupLabel.copy(color = c.textMuted),
                                modifier = Modifier.padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 14.dp,
                                    bottom = 4.dp,
                                ),
                            )
                        }
                        items(
                            items = bucket,
                            key = { it.publicKey },
                        ) { contact ->
                            ContactListItem(
                                contact = contact,
                                onChat = { onStartChat(contact.publicKey) },
                            )
                        }
                    }
                }
            }
        }

        ZappFab(
            icon = Icons.Default.PersonAdd,
            contentDescription = "Add contact",
            onClick = { showAddDialog = true },
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

    if (showAddDialog) {
        AddContactSheet(
            existingKeys = contacts.map { it.publicKey }.toSet(),
            scannedPublicKey = scannedPublicKey,
            onScanQr = { viewModel.scanPublicKey() },
            onConsumeScannedKey = { viewModel.consumeScannedKey() },
            onDismiss = { showAddDialog = false },
            onAdd = { publicKey, name ->
                viewModel.addContact(publicKey, name)
                viewModel.consumeScannedKey()
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ContactListItem(
    contact: ChatContact,
    onChat: () -> Unit,
) {
    val c = ZappTheme.colors
    val initials = remember(contact.name) { initialsOf(contact.name) }
    val shortKey = remember(contact.publicKey) { contact.publicKey.ellipsizeAddress() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onChat)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(c.accent, RectangleShape),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = initials,
                style = ZappTheme.typography.rowTitle.copy(color = c.onAccent),
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = contact.name,
                style = ZappTheme.typography.rowTitle.copy(color = c.text),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                text = shortKey,
                style = ZappTheme.typography.mono.copy(color = c.textMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onChat)
                .semantics { contentDescription = "Start chat"; role = Role.Button },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Chat,
                contentDescription = null,
                tint = c.accent,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddContactSheet(
    existingKeys: Set<String>,
    scannedPublicKey: String?,
    onScanQr: () -> Unit,
    onConsumeScannedKey: () -> Unit,
    onDismiss: () -> Unit,
    onAdd: (publicKey: String, name: String) -> Unit,
) {
    val c = ZappTheme.colors
    var nameInput by remember { mutableStateOf(TextFieldValue("")) }
    var publicKeyInput by remember { mutableStateOf(TextFieldValue("")) }
    var error by remember { mutableStateOf<String?>(null) }

    // When a scan result arrives via the VM, populate the input and consume it
    // so re-opening the sheet later doesn't pre-fill stale data.
    LaunchedEffect(scannedPublicKey) {
        scannedPublicKey?.let { key ->
            publicKeyInput = TextFieldValue(key)
            error = null
            onConsumeScannedKey()
        }
    }

    val cleanedKey = publicKeyInput.text.trim().removePrefix("0x")
    val isValidKey = cleanedKey.length == 64 &&
        cleanedKey.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        shape = RectangleShape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .imePadding()
                .padding(bottom = 28.dp),
        ) {
            BasicText(
                text = "Add Contact",
                style = ZappTheme.typography.sectionTitle.copy(
                    color = c.text,
                    fontWeight = FontWeight.Black,
                ),
            )

            Spacer(Modifier.height(20.dp))

            ContactInputField(
                value = nameInput,
                onValueChange = { nameInput = it; error = null },
                placeholder = "Name",
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = c.textSubtle,
                    )
                },
            )

            Spacer(Modifier.height(12.dp))

            ContactInputField(
                value = publicKeyInput,
                onValueChange = { publicKeyInput = it; error = null },
                placeholder = "Public Key (64 hex chars)",
                leadingIcon = {
                    Icon(
                        Icons.Default.Key,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = c.textSubtle,
                    )
                },
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(onClick = onScanQr)
                            .semantics { contentDescription = "Scan QR code"; role = Role.Button },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = c.textSubtle,
                        )
                    }
                },
            )

            // Valid key confirmation row
            if (isValidKey) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.successSoft, RectangleShape)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = c.success,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    BasicText(
                        text = "${cleanedKey.take(10)}…${cleanedKey.takeLast(6)}",
                        style = ZappTheme.typography.chip.copy(color = c.success),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Inline error message
            error?.let {
                Spacer(Modifier.height(8.dp))
                BasicText(
                    text = it,
                    style = ZappTheme.typography.caption.copy(color = c.danger),
                )
            }

            Spacer(Modifier.height(20.dp))

            // Add Contact primary CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(c.accent, RectangleShape)
                    .clickable(onClick = {
                        val pk = publicKeyInput.text.trim().removePrefix("0x")
                        val name = nameInput.text.trim()
                        val isValidHex = pk.length == 64 && pk.all {
                            it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F'
                        }
                        when {
                            name.isEmpty() -> error = "Name is required"
                            pk.isEmpty() -> error = "Public key is required"
                            !isValidHex -> error = "Invalid public key — must be 64 hex characters"
                            existingKeys.contains(pk) -> error = "Contact already exists"
                            else -> onAdd(pk, name)
                        }
                    })
                    .semantics { contentDescription = "Add Contact"; role = Role.Button },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "ADD CONTACT",
                    style = ZappTheme.typography.button.copy(
                        color = c.onAccent,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ContactInputField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val c = ZappTheme.colors
    val isFilled = value.text.isNotEmpty()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceInput, RectangleShape)
            .then(
                if (isFilled) {
                    Modifier.border(BorderStroke(2.dp, c.borderStrong), RectangleShape)
                } else {
                    Modifier.border(BorderStroke(1.dp, c.border), RectangleShape)
                }
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 0.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let {
                it()
                Spacer(Modifier.width(10.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = ZappTheme.typography.body.copy(color = c.text),
                    cursorBrush = SolidColor(c.accent),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (value.text.isEmpty()) {
                    BasicText(
                        text = placeholder,
                        style = ZappTheme.typography.body.copy(color = c.textSubtle),
                    )
                }
            }
            trailingIcon?.let { it() }
        }
    }
}
