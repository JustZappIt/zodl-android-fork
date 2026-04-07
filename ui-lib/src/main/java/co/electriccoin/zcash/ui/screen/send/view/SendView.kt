@file:Suppress("TooManyFunctions")

package co.electriccoin.zcash.ui.screen.send.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cash.z.ecc.android.sdk.model.Memo
import cash.z.ecc.android.sdk.model.WalletAddress
import cash.z.ecc.android.sdk.model.ZecSend
import cash.z.ecc.android.sdk.model.ZecSendExt
import cash.z.ecc.android.sdk.type.AddressType
import cash.z.ecc.sdk.type.ZcashCurrency
import co.electriccoin.zcash.spackle.Twig
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.common.appbar.ZashiMainTopAppBarState
import co.electriccoin.zcash.ui.common.model.WalletAccount
import co.electriccoin.zcash.ui.common.wallet.ExchangeRateState
import co.electriccoin.zcash.ui.design.component.AppAlertDialog
import co.electriccoin.zcash.ui.design.component.BlankSurface
import co.electriccoin.zcash.ui.design.component.Spacer
import co.electriccoin.zcash.ui.design.component.ZashiTextField
import co.electriccoin.zcash.ui.design.component.ZashiTextFieldDefaults
import co.electriccoin.zcash.ui.design.component.zapp.ZappBottomActionBar
import co.electriccoin.zcash.ui.design.component.zapp.ZappButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.design.theme.ZcashTheme
import co.electriccoin.zcash.ui.design.util.getString
import co.electriccoin.zcash.ui.design.util.getValue
import co.electriccoin.zcash.ui.design.util.rememberDesiredFormatLocale
import co.electriccoin.zcash.ui.screen.balances.BalanceWidget
import co.electriccoin.zcash.ui.screen.balances.BalanceWidgetState
import co.electriccoin.zcash.ui.screen.send.SendTag
import co.electriccoin.zcash.ui.screen.send.model.AmountState
import co.electriccoin.zcash.ui.screen.send.model.MemoState
import co.electriccoin.zcash.ui.screen.send.model.RecipientAddressState
import co.electriccoin.zcash.ui.screen.send.model.SendAddressBookState
import co.electriccoin.zcash.ui.screen.send.model.SendStage
import kotlinx.coroutines.launch

// TODO [#1260]: Cover Send screens UI with tests
// TODO [#1260]: https://github.com/Electric-Coin-Company/zashi-android/issues/1260
@Suppress("LongParameterList")
@Composable
fun Send(
    balanceWidgetState: BalanceWidgetState,
    sendStage: SendStage,
    onCreateZecSend: (ZecSend) -> Unit,
    onBack: () -> Unit,
    onQrScannerOpen: () -> Unit,
    hasCameraFeature: Boolean,
    recipientAddressState: RecipientAddressState,
    onRecipientAddressChange: (String) -> Unit,
    setAmountState: (AmountState) -> Unit,
    amountState: AmountState,
    setMemoState: (MemoState) -> Unit,
    memoState: MemoState,
    selectedAccount: WalletAccount?,
    exchangeRateState: ExchangeRateState,
    sendAddressBookState: SendAddressBookState,
    zashiMainTopAppBarState: ZashiMainTopAppBarState?,
) {
    if (selectedAccount == null) {
        return
    }

    val c = ZappTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.displayCutout))
            .imePadding(),
    ) {
        ZappScreenHeader(title = "Send")
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            SendMainContent(
                balanceWidgetState = balanceWidgetState,
                selectedAccount = selectedAccount,
                exchangeRateState = exchangeRateState,
                onBack = onBack,
                onCreateZecSend = onCreateZecSend,
                sendStage = sendStage,
                onQrScannerOpen = onQrScannerOpen,
                recipientAddressState = recipientAddressState,
                onRecipientAddressChange = onRecipientAddressChange,
                hasCameraFeature = hasCameraFeature,
                amountState = amountState,
                setAmountState = setAmountState,
                memoState = memoState,
                setMemoState = setMemoState,
                sendState = sendAddressBookState,
                modifier = Modifier.padding(horizontal = 28.dp),
            )
        }
        ZappBottomActionBar(onBack = onBack)
    }
}

@Suppress("LongParameterList")
@Composable
private fun SendMainContent(
    balanceWidgetState: BalanceWidgetState,
    selectedAccount: WalletAccount,
    exchangeRateState: ExchangeRateState,
    onBack: () -> Unit,
    onCreateZecSend: (ZecSend) -> Unit,
    sendStage: SendStage,
    onQrScannerOpen: () -> Unit,
    recipientAddressState: RecipientAddressState,
    onRecipientAddressChange: (String) -> Unit,
    hasCameraFeature: Boolean,
    amountState: AmountState,
    setAmountState: (AmountState) -> Unit,
    memoState: MemoState,
    setMemoState: (MemoState) -> Unit,
    sendState: SendAddressBookState,
    modifier: Modifier = Modifier,
) {
    // For now, we merge [SendStage.Form] and [SendStage.Proposing] into one stage. We could eventually display a
    // loader if calling the Proposal API takes longer than expected

    SendForm(
        balanceWidgetState = balanceWidgetState,
        selectedAccount = selectedAccount,
        recipientAddressState = recipientAddressState,
        exchangeRateState = exchangeRateState,
        onRecipientAddressChange = onRecipientAddressChange,
        amountState = amountState,
        setAmountState = setAmountState,
        memoState = memoState,
        setMemoState = setMemoState,
        onCreateZecSend = onCreateZecSend,
        onQrScannerOpen = onQrScannerOpen,
        hasCameraFeature = hasCameraFeature,
        sendState = sendState,
        modifier = modifier
    )

    if (sendStage is SendStage.SendFailure) {
        SendFailure(
            reason = sendStage.error,
            onConfirm = onBack
        )
    }
}

// TODO [#217]: Need to handle changing of Locale after user input, but before submitting the button.
// TODO [#217]: https://github.com/Electric-Coin-Company/zashi-android/issues/217

// TODO [#1257]: Send.Form TextFields not persisted on a configuration change when the underlying ViewPager is on the
//  Balances page
// TODO [#1257]: https://github.com/Electric-Coin-Company/zashi-android/issues/1257
@Suppress("LongParameterList", "LongMethod")
@Composable
private fun SendForm(
    balanceWidgetState: BalanceWidgetState,
    selectedAccount: WalletAccount,
    recipientAddressState: RecipientAddressState,
    exchangeRateState: ExchangeRateState,
    onRecipientAddressChange: (String) -> Unit,
    amountState: AmountState,
    setAmountState: (AmountState) -> Unit,
    memoState: MemoState,
    setMemoState: (MemoState) -> Unit,
    onCreateZecSend: (ZecSend) -> Unit,
    onQrScannerOpen: () -> Unit,
    hasCameraFeature: Boolean,
    sendState: SendAddressBookState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(8.dp)

        BalanceWidget(
            state = balanceWidgetState
        )

        Spacer(24.dp)

        // TODO [#1256]: Consider Send.Form TextFields scrolling
        // TODO [#1256]: https://github.com/Electric-Coin-Company/zashi-android/issues/1256

        SendFormAddressTextField(
            hasCameraFeature = hasCameraFeature,
            onQrScannerOpen = onQrScannerOpen,
            recipientAddressState = recipientAddressState,
            setRecipientAddress = onRecipientAddressChange,
            sendAddressBookState = sendState
        )

        AnimatedVisibility(visible = sendState.isHintVisible) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                SendAddressBookHint(Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.size(16.dp))

        val isMemoFieldAvailable =
            recipientAddressState.address.isEmpty() ||
                recipientAddressState.type is AddressType.Invalid ||
                (
                    recipientAddressState.type is AddressType.Valid &&
                        recipientAddressState.type !is AddressType.Transparent &&
                        recipientAddressState.type !is AddressType.Tex
                )

        SendFormAmountTextField(
            amountState = amountState,
            imeAction =
                if (recipientAddressState.type == AddressType.Transparent || !isMemoFieldAvailable) {
                    ImeAction.Done
                } else {
                    ImeAction.Next
                },
            isTransparentOrTextRecipient =
                recipientAddressState.type?.let {
                    it == AddressType.Transparent || it == AddressType.Tex
                } ?: false,
            exchangeRateState = exchangeRateState,
            setAmountState = setAmountState,
            selectedAccount = selectedAccount
        )

        Spacer(Modifier.size(16.dp))

        SendFormMemoTextField(
            memoState = memoState,
            setMemoState = setMemoState,
            isMemoFieldAvailable = isMemoFieldAvailable,
        )

        Spacer(
            modifier =
                Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        SendButton(
            amountState = amountState,
            memoState = memoState,
            onCreateZecSend = onCreateZecSend,
            recipientAddressState = recipientAddressState,
            selectedAccount = selectedAccount,
        )
    }
}

@Suppress("CyclomaticComplexMethod")
@Composable
fun SendButton(
    amountState: AmountState,
    memoState: MemoState,
    onCreateZecSend: (ZecSend) -> Unit,
    recipientAddressState: RecipientAddressState,
    selectedAccount: WalletAccount,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val locale = rememberDesiredFormatLocale()

    // Common conditions continuously checked for validity
    val sendButtonEnabled =
        recipientAddressState.type !is AddressType.Invalid &&
            recipientAddressState.address.isNotEmpty() &&
            amountState is AmountState.Valid &&
            amountState.value.getValue().isNotBlank() &&
            selectedAccount.canSpend(amountState.zatoshi) &&
            // A valid memo is necessary only for non-transparent recipient
            (recipientAddressState.type == AddressType.Transparent || memoState is MemoState.Correct)

    ZappButton(
        text = stringResource(id = R.string.send_create),
        enabled = sendButtonEnabled,
        modifier = Modifier
            .testTag(SendTag.SEND_FORM_BUTTON)
            .fillMaxWidth(),
        onClick = {
            scope.launch {
                // SDK side validations
                val zecSendValidation =
                    ZecSendExt.new(
                        locale = locale,
                        destinationString = recipientAddressState.address,
                        zecString = amountState.value.getString(context),
                        // Take memo for a valid non-transparent receiver only
                        memoString =
                            if (recipientAddressState.type == AddressType.Transparent) {
                                ""
                            } else {
                                memoState.text
                            },
                    )

                when (zecSendValidation) {
                    is ZecSendExt.ZecSendValidation.Valid -> {
                        onCreateZecSend(
                            zecSendValidation.zecSend.copy(
                                destination =
                                    when (recipientAddressState.type) {
                                        is AddressType.Invalid -> {
                                            WalletAddress.Unified.new(recipientAddressState.address)
                                        }

                                        AddressType.Shielded -> {
                                            WalletAddress.Unified.new(recipientAddressState.address)
                                        }

                                        AddressType.Tex -> {
                                            WalletAddress.Tex.new(recipientAddressState.address)
                                        }

                                        AddressType.Transparent -> {
                                            WalletAddress.Transparent.new(recipientAddressState.address)
                                        }

                                        AddressType.Unified -> {
                                            WalletAddress.Unified.new(recipientAddressState.address)
                                        }

                                        null -> {
                                            WalletAddress.Unified.new(recipientAddressState.address)
                                        }
                                    }
                            )
                        )
                    }

                    is ZecSendExt.ZecSendValidation.Invalid -> {
                        // We do not expect this validation to fail, so logging is enough here
                        // An error popup could be reasonable here as well
                        Twig.warn { "Send failed with: ${zecSendValidation.validationErrors}" }
                    }
                }
            }
        },
    )
}

@Suppress("LongMethod")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SendFormAddressTextField(
    sendAddressBookState: SendAddressBookState,
    hasCameraFeature: Boolean,
    onQrScannerOpen: () -> Unit,
    recipientAddressState: RecipientAddressState,
    setRecipientAddress: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    Column(
        modifier =
            Modifier
                // Animate error show/hide
                .animateContentSize()
                // Scroll TextField above ime keyboard
                .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        BasicText(
            text = stringResource(id = R.string.send_address_label),
            style = ZappTheme.typography.body.copy(color = ZappTheme.colors.textMuted),
        )

        Spacer(modifier = Modifier.height(8.dp))

        val recipientAddressValue = recipientAddressState.address
        val recipientAddressError =
            if (
                recipientAddressValue.isNotEmpty() &&
                recipientAddressState.type is AddressType.Invalid
            ) {
                stringResource(id = R.string.send_address_invalid)
            } else {
                null
            }

        ZashiTextField(
            singleLine = true,
            maxLines = 1,
            value = recipientAddressValue,
            onValueChange = {
                setRecipientAddress(it)
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onFocusEvent { focusState ->
                        if (focusState.isFocused) {
                            scope.launch {
                                bringIntoViewRequester.bringIntoView()
                            }
                        }
                    },
            error = recipientAddressError,
            textStyle = ZappTheme.typography.body.copy(fontFamily = ZappTheme.typography.mono.fontFamily),
            placeholder = {
                BasicText(
                    text = stringResource(id = R.string.send_address_hint),
                    style = ZappTheme.typography.body.copy(color = ZappTheme.colors.textSubtle),
                )
            },
            suffix =
                if (hasCameraFeature) {
                    {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable(
                                        onClick = sendAddressBookState.onButtonClick,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true),
                                    )
                                    .semantics(mergeDescendants = true) { role = Role.Button },
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    painter = painterResource(sendAddressBookState.mode.icon),
                                    contentDescription = stringResource(R.string.send_address_book_content_description),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable(
                                        onClick = onQrScannerOpen,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true),
                                    )
                                    .semantics(mergeDescendants = true) { role = Role.Button },
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.qr_code_icon),
                                    contentDescription = stringResource(R.string.send_scan_content_description),
                                )
                            }
                        }
                    }
                } else {
                    null
                },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
            keyboardActions =
                KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Next)
                    }
                ),
        )
    }
}

@Suppress("LongParameterList", "LongMethod")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SendFormAmountTextField(
    amountState: AmountState,
    imeAction: ImeAction,
    isTransparentOrTextRecipient: Boolean,
    exchangeRateState: ExchangeRateState,
    setAmountState: (AmountState) -> Unit,
    selectedAccount: WalletAccount,
) {
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current

    val zcashCurrency = ZcashCurrency.getLocalizedName(context)

    val locale = rememberDesiredFormatLocale()

    val amountError =
        when (amountState) {
            is AmountState.Invalid -> {
                if (amountState.value.isEmpty()) {
                    null
                } else {
                    stringResource(id = R.string.send_amount_invalid)
                }
            }

            is AmountState.Valid -> {
                if (selectedAccount.spendableShieldedBalance < amountState.zatoshi) {
                    stringResource(id = R.string.send_amount_insufficient_balance)
                } else {
                    null
                }
            }
        }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    Column(
        modifier =
            Modifier
                // Animate error show/hide
                .animateContentSize()
                // Scroll TextField above ime keyboard
                .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        BasicText(
            text = stringResource(id = R.string.send_amount_label),
            style = ZappTheme.typography.body.copy(color = ZappTheme.colors.textMuted),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            ZashiTextField(
                singleLine = true,
                maxLines = 1,
                value = amountState.value.getString(context),
                onValueChange = { newValue ->
                    setAmountState(
                        AmountState.newFromZec(
                            value = newValue,
                            fiatValue = amountState.fiatValue.getString(context),
                            isTransparentOrTextRecipient = isTransparentOrTextRecipient,
                            exchangeRateState = exchangeRateState,
                            locale = locale
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                innerModifier =
                    ZashiTextFieldDefaults
                        .innerModifier
                        .testTag(SendTag.SEND_AMOUNT_FIELD),
                error = amountError,
                placeholder = {
                    BasicText(
                        text = stringResource(
                            id = R.string.send_amount_hint,
                            zcashCurrency
                        ),
                        style = ZappTheme.typography.body.copy(color = ZappTheme.colors.textSubtle),
                    )
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = imeAction
                    ),
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                            focusManager.clearFocus(true)
                        },
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                prefix = {
                    Image(
                        painter = painterResource(R.drawable.ic_send_zashi),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(color = ZappTheme.colors.textSubtle),
                    )
                }
            )

            if (exchangeRateState is ExchangeRateState.Data) {
                Spacer(modifier = Modifier.width(12.dp))
                Image(
                    modifier = Modifier.padding(top = 12.dp),
                    painter = painterResource(id = R.drawable.ic_send_convert),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = ZappTheme.colors.accent),
                )
                Spacer(modifier = Modifier.width(12.dp))
                ZashiTextField(
                    singleLine = true,
                    maxLines = 1,
                    isEnabled = !exchangeRateState.isStale && exchangeRateState.currencyConversion != null,
                    value = amountState.fiatValue.getString(context),
                    onValueChange = { newValue ->
                        setAmountState(
                            AmountState.newFromFiat(
                                value = amountState.value.getString(context),
                                fiatValue = newValue,
                                isTransparentOrTextRecipient = isTransparentOrTextRecipient,
                                exchangeRateState = exchangeRateState,
                                locale = locale
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        BasicText(
                            text = stringResource(id = R.string.send_usd_amount_hint),
                            style = ZappTheme.typography.body.copy(color = ZappTheme.colors.textSubtle),
                        )
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = imeAction
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                focusManager.clearFocus(true)
                            },
                            onNext = {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        ),
                    prefix = {
                        Image(
                            painter = painterResource(R.drawable.ic_send_usd),
                            contentDescription = null,
                            colorFilter =
                                if (!exchangeRateState.isStale) {
                                    ColorFilter.tint(color = ZappTheme.colors.textSubtle)
                                } else {
                                    ColorFilter.tint(color = ZappTheme.colors.textSubtle)
                                }
                        )
                    }
                )
            }
        }
    }
}

@Suppress("LongMethod")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SendFormMemoTextField(
    isMemoFieldAvailable: Boolean,
    memoState: MemoState,
    setMemoState: (MemoState) -> Unit,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    Column(
        modifier =
            Modifier
                // Animate error show/hide
                .animateContentSize()
                // Scroll TextField above ime keyboard
                .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        BasicText(
            text = stringResource(id = R.string.send_memo_label),
            style = ZappTheme.typography.body.copy(color = ZappTheme.colors.textMuted),
        )

        Spacer(modifier = Modifier.height(8.dp))

        ZashiTextField(
            minLines = if (isMemoFieldAvailable) 4 else 1,
            isEnabled = isMemoFieldAvailable,
            value =
                if (isMemoFieldAvailable) {
                    memoState.text
                } else {
                    ""
                },
            error = if (memoState is MemoState.Correct) null else "",
            onValueChange = {
                setMemoState(MemoState.new(it))
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default,
                    capitalization = KeyboardCapitalization.Sentences
                ),
            placeholder = {
                if (isMemoFieldAvailable) {
                    BasicText(
                        text = stringResource(id = R.string.send_memo_hint),
                        style = ZappTheme.typography.body.copy(color = ZappTheme.colors.textSubtle),
                    )
                } else {
                    BasicText(
                        text = stringResource(R.string.send_transparent_memo),
                        style = ZappTheme.typography.caption.copy(color = ZappTheme.colors.textMuted),
                    )
                }
            },
            leadingIcon =
                if (isMemoFieldAvailable) {
                    null
                } else {
                    {
                        Image(
                            painter = painterResource(id = R.drawable.ic_confirmation_message_info),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(ZappTheme.colors.textMuted)
                        )
                    }
                },
            colors =
                if (isMemoFieldAvailable) {
                    ZashiTextFieldDefaults.defaultColors()
                } else {
                    ZashiTextFieldDefaults.defaultColors(
                        disabledTextColor = ZappTheme.colors.textSubtle,
                        disabledHintColor = ZappTheme.colors.textSubtle,
                        disabledBorderColor = Color.Unspecified,
                        disabledContainerColor = ZappTheme.colors.surfaceAlt,
                        disabledPlaceholderColor = ZappTheme.colors.textSubtle,
                    )
                },
            modifier = Modifier.fillMaxWidth(),
        )

        if (isMemoFieldAvailable) {
            BasicText(
                text = stringResource(
                    id = R.string.send_memo_bytes_counter,
                    Memo.MAX_MEMO_LENGTH_BYTES - memoState.byteSize,
                    Memo.MAX_MEMO_LENGTH_BYTES
                ),
                style = ZappTheme.typography.caption.copy(
                    color = if (memoState is MemoState.Correct) {
                        ZappTheme.colors.textSubtle
                    } else {
                        ZappTheme.colors.danger
                    },
                    textAlign = TextAlign.End,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            )
        }
    }
}

@Composable
@Preview("SendFailure")
private fun PreviewSendFailure() {
    ZcashTheme(forceDarkMode = false) {
        BlankSurface {
            SendFailure(
                onConfirm = {},
                reason = "Insufficient balance"
            )
        }
    }
}

@Composable
private fun SendFailure(
    onConfirm: () -> Unit,
    reason: String,
    modifier: Modifier = Modifier
) {
    // TODO [#1276]: Once we ensure that the reason contains a localized message, we can leverage it for the UI prompt
    // TODO [#1276]: Consider adding support for a specific exception in AppAlertDialog
    // TODO [#1276]: https://github.com/Electric-Coin-Company/zashi-android/issues/1276

    AppAlertDialog(
        title = stringResource(id = R.string.send_dialog_error_title),
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState())
            ) {
                BasicText(
                    text = stringResource(id = R.string.send_dialog_error_text),
                    style = ZappTheme.typography.body.copy(color = ZappTheme.colors.text),
                )

                Spacer(modifier = Modifier.height(16.dp))

                BasicText(
                    text = reason,
                    style = ZappTheme.typography.body.copy(
                        fontStyle = FontStyle.Italic,
                        color = ZappTheme.colors.text,
                    ),
                )
            }
        },
        confirmButtonText = stringResource(id = R.string.send_dialog_error_btn),
        onConfirmButtonClick = onConfirm,
        modifier = modifier
    )
}
