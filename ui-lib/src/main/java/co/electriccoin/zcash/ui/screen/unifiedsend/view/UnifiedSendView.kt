@file:Suppress("TooManyFunctions")

package co.electriccoin.zcash.ui.screen.unifiedsend.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.design.component.AssetCardState
import co.electriccoin.zcash.ui.design.component.ChipButtonState
import co.electriccoin.zcash.ui.design.component.Spacer
import co.electriccoin.zcash.ui.design.component.ZashiAddressTextField
import co.electriccoin.zcash.ui.design.component.ZashiAssetCard
import co.electriccoin.zcash.ui.design.component.ZashiChipButton
import co.electriccoin.zcash.ui.design.component.ZashiImageButton
import co.electriccoin.zcash.ui.design.component.ZashiNumberTextField
import co.electriccoin.zcash.ui.design.component.ZashiNumberTextFieldDefaults
import co.electriccoin.zcash.ui.design.component.ZashiTextField
import co.electriccoin.zcash.ui.design.component.ZashiTextFieldDefaults
import co.electriccoin.zcash.ui.design.component.IconButtonState
import co.electriccoin.zcash.ui.design.component.zapp.ZappBottomActionBar
import co.electriccoin.zcash.ui.design.component.zapp.ZappButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappButtonVariant
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.design.theme.ZcashTheme
import co.electriccoin.zcash.ui.design.theme.colors.ZashiColors
import co.electriccoin.zcash.ui.design.theme.typography.ZashiTypography
import co.electriccoin.zcash.ui.design.util.StringResource
import co.electriccoin.zcash.ui.design.util.getValue
import co.electriccoin.zcash.ui.screen.balances.BalanceWidget
import co.electriccoin.zcash.ui.screen.balances.BalanceWidgetState
import co.electriccoin.zcash.ui.screen.unifiedsend.view.SendAddressBookHint
import co.electriccoin.zcash.ui.screen.swap.SlippageButton
import co.electriccoin.zcash.ui.screen.swap.SwapErrorFooter
import co.electriccoin.zcash.ui.screen.unifiedsend.model.MemoFieldState
import co.electriccoin.zcash.ui.screen.unifiedsend.model.PrimaryButtonState
import co.electriccoin.zcash.ui.screen.unifiedsend.model.UnifiedSendFormState

@Composable
internal fun UnifiedSendView(
    state: UnifiedSendFormState,
    balanceState: BalanceWidgetState,
) {
    val c = ZappTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.displayCutout))
            .imePadding(),
    ) {
        ZappScreenHeader(title = stringResource(R.string.unified_send_title))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 8.dp),
            ) {
                // Balance widget
                Spacer(8.dp)
                BalanceWidget(
                    modifier = Modifier.align(CenterHorizontally),
                    state = balanceState
                )
                Spacer(32.dp)

                // ── Sentence: "I want to send" ──────────────────────────────
                SentenceFragment(stringResource(R.string.unified_send_sentence_i_want_to_send))
                Spacer(8.dp)
                AddressField(state)
                AnimatedVisibility(visible = state.isABHintVisible) {
                    Column {
                        Spacer(8.dp)
                        SendAddressBookHint(Modifier.fillMaxWidth())
                    }
                }
                Spacer(20.dp)

                // ── Sentence: "who will receive" ────────────────────────────
                SentenceFragment(stringResource(R.string.unified_send_sentence_who_will_receive))
                Spacer(8.dp)
                ZashiAssetCard(state.asset)
                Spacer(20.dp)

                // ── Sentence: "I'll pay" ─────────────────────────────────────
                SentenceFragment(stringResource(R.string.unified_send_sentence_ill_pay))
                Spacer(8.dp)
                AmountFields(state)
                if (state.amountError != null) {
                    Spacer(height = 6.dp)
                    Text(
                        text = state.amountError.getValue(),
                        style = ZashiTypography.textSm,
                        color = ZashiColors.Inputs.ErrorDefault.hint,
                    )
                }
                Spacer(12.dp)

                // ── Swap mode: "They receive ≈ X TOKEN" ─────────────────────
                if (state.theyReceiveLabel != null) {
                    TheyReceiveRow(state.theyReceiveLabel)
                    Spacer(8.dp)
                }

                // ── Swap mode: Slippage ──────────────────────────────────────
                if (state.slippage != null && state.onSlippageClick != null) {
                    SlippageButton(
                        state = co.electriccoin.zcash.ui.design.component.ButtonState(
                            text = state.slippage,
                            icon = R.drawable.ic_swap_slippage,
                            onClick = state.onSlippageClick,
                        )
                    )
                    Spacer(8.dp)
                }

                // ── ZEC-direct mode: Memo ────────────────────────────────────
                val memo = state.memo
                AnimatedVisibility(visible = memo != null) {
                    if (memo is MemoFieldState.Editable) MemoSection(memo)
                }

                // ── Footers ──────────────────────────────────────────────────
                if (state.errorFooter != null) {
                    Spacer(1f)
                    Spacer(12.dp)
                    SwapErrorFooter(state.errorFooter)
                    Spacer(24.dp)
                } else if (state.infoFooter != null) {
                    Spacer(1f)
                    Spacer(12.dp)
                    Text(
                        text = state.infoFooter.getValue(),
                        style = ZashiTypography.textSm,
                        color = ZashiColors.Text.textSecondary,
                    )
                    Spacer(24.dp)
                } else {
                    Spacer(1f)
                    Spacer(12.dp)
                }
            }
        }

        // Bottom action bar: back on left, CTA on right
        ZappBottomActionBar(
            onBack = state.onBack,
            primaryAction = { CtaButton(state.primaryButton) }
        )
    }
}

@Composable
private fun SentenceFragment(text: String) {
    BasicText(
        text = text,
        style = ZappTheme.typography.caption.copy(color = ZappTheme.colors.textMuted),
    )
}

@Composable
private fun CtaButton(btn: PrimaryButtonState) {
    when (btn) {
        is PrimaryButtonState.Review -> ZappButton(
            text = stringResource(R.string.send_create),
            enabled = !btn.isLoading,
            onClick = btn.onClick,
        )

        is PrimaryButtonState.TopUp -> ZappButton(
            text = stringResource(R.string.unified_send_top_up),
            variant = ZappButtonVariant.Secondary,
            onClick = btn.onClick,
        )

        PrimaryButtonState.Disabled -> ZappButton(
            text = stringResource(R.string.send_create),
            enabled = false,
            onClick = {},
        )
    }
}

@Composable
private fun TheyReceiveRow(label: StringResource) {
    Row(verticalAlignment = CenterVertically) {
        Image(
            modifier = Modifier.size(20.dp),
            painter = painterResource(R.drawable.ic_zec_round_full),
            contentDescription = null
        )
        Spacer(8.dp)
        Text(
            text = label.getValue(),
            style = ZashiTypography.textSm,
            fontWeight = FontWeight.Medium,
            color = ZashiColors.Text.textPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AmountFields(state: UnifiedSendFormState) {
    Row(verticalAlignment = CenterVertically) {
        val amountSource = remember { MutableInteractionSource() }
        val isAmountFocused by amountSource.collectIsFocusedAsState()

        ZashiNumberTextField(
            modifier = Modifier.weight(1f),
            state = state.zecAmount,
            interactionSource = amountSource,
            placeholder = if (!isAmountFocused) {
                {
                    ZashiNumberTextFieldDefaults.Placeholder(
                        modifier = Modifier.fillMaxWidth(),
                        style = ZashiTypography.textMd.copy(color = ZashiColors.Inputs.Default.text),
                        fontWeight = FontWeight.Normal,
                        text = "0.00"
                    )
                }
            } else {
                null
            },
        )

        Spacer(12.dp)
        Image(
            painter = painterResource(R.drawable.ic_send_convert),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = ZcashTheme.colors.secondaryColor),
        )
        Spacer(12.dp)

        ZashiNumberTextField(
            modifier = Modifier.weight(1f),
            state = state.fiatAmount,
            placeholder = {
                ZashiNumberTextFieldDefaults.Placeholder(
                    modifier = Modifier.fillMaxWidth(),
                    style = ZashiTypography.textMd.copy(color = ZashiColors.Inputs.Default.text),
                    fontWeight = FontWeight.Normal,
                    text = stringResource(R.string.send_usd_amount_hint)
                )
            },
            prefix = {
                Image(
                    painter = painterResource(R.drawable.ic_send_usd),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        if (state.fiatAmount.innerState.innerTextFieldState.value.getValue().isNotEmpty()) {
                            ZashiColors.Inputs.Filled.text
                        } else {
                            ZashiColors.Inputs.Filled.iconMain
                        }
                    )
                )
            }
        )
    }
}

@Composable
private fun AddressField(state: UnifiedSendFormState) {
    ZashiAddressTextField(
        state = state.address,
        modifier = Modifier
            .fillMaxWidth()
            .onKeyEvent {
                if (state.abContact != null && it.nativeKeyEvent.keyCode == NativeKeyEvent.KEYCODE_DEL) {
                    state.abContact.onClick()
                    true
                } else {
                    false
                }
            },
        placeholder = if (state.abContact == null) {
            {
                Text(
                    text = state.addressPlaceholder.getValue(),
                    style = ZashiTypography.textMd,
                    color = ZashiColors.Inputs.Default.text,
                )
            }
        } else {
            null
        },
        prefix = if (state.abContact == null) {
            null
        } else {
            { ContactChipPrefix(state.abContact) }
        },
        suffix = {
            Row(verticalAlignment = Alignment.Top) {
                ZashiImageButton(modifier = Modifier.size(36.dp), state = state.abButton)
                Spacer(4.dp)
                ZashiImageButton(modifier = Modifier.size(36.dp), state = state.qrButton)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
        ),
    )
}

@Composable
private fun ContactChipPrefix(contact: ChipButtonState) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 3.5.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        ZashiChipButton(
            contact,
            contentPadding = PaddingValues(start = 10.dp, top = 4.5.dp, end = 4.5.dp, bottom = 4.5.dp),
            useTint = false,
            shape = RoundedCornerShape(0.dp),
            color = ZashiColors.Tags.surfacePrimary,
            border = BorderStroke(1.dp, ZashiColors.Tags.surfaceStroke),
            textStyle = ZashiTypography.textSm.copy(
                color = ZashiColors.Text.textPrimary,
                fontWeight = FontWeight.Medium,
            )
        )
    }
}

@Composable
private fun MemoSection(memo: MemoFieldState.Editable) {
    Column {
        Spacer(12.dp)
        BasicText(
            text = stringResource(R.string.unified_send_memo_hint),
            style = ZappTheme.typography.caption.copy(color = ZappTheme.colors.textMuted),
        )
        Spacer(8.dp)
        ZashiTextField(
            value = memo.text,
            onValueChange = memo.onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
            singleLine = false,
            maxLines = 4,
            isEnabled = memo.isEnabled,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default,
            ),
            innerModifier = ZashiTextFieldDefaults.innerModifier,
            placeholder = null,
            suffix = {
                Text(
                    text = "${memo.byteCount} / ${memo.maxBytes}",
                    style = ZashiTypography.textXs,
                    color = if (memo.byteCount > memo.maxBytes) {
                        ZashiColors.Inputs.ErrorDefault.hint
                    } else {
                        ZashiColors.Text.textTertiary
                    },
                )
            },
        )
    }
}
