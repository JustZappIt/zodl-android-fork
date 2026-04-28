package co.electriccoin.zcash.ui.screen.receive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.common.appbar.ZashiMainTopAppBarState
import co.electriccoin.zcash.ui.design.component.CircularScreenProgressIndicator
import co.electriccoin.zcash.ui.design.component.IconButtonState
import co.electriccoin.zcash.ui.design.component.ZashiImageButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappBottomActionBar
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.newcomponent.PreviewScreens
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.design.theme.ZcashTheme
import co.electriccoin.zcash.ui.design.util.getValue
import co.electriccoin.zcash.ui.design.util.stringRes
import co.electriccoin.zcash.ui.design.util.styledStringResource
import co.electriccoin.zcash.ui.fixture.ZashiMainTopAppBarStateFixture
import co.electriccoin.zcash.ui.util.CURRENCY_TICKER

@Composable
internal fun ReceiveView(
    state: ReceiveState,
    appBarState: ZashiMainTopAppBarState?,
) {
    when {
        state.items.isNullOrEmpty() && state.isLoading -> {
            CircularScreenProgressIndicator()
        }

        else -> {
            val c = ZappTheme.colors
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(c.bg)
                    .windowInsetsPadding(WindowInsets.statusBars),
            ) {
                ZappScreenHeader(title = stringResource(R.string.receive_title, CURRENCY_TICKER))

                ReceiveContents(
                    items = state.items.orEmpty(),
                    modifier = Modifier.weight(1f),
                )

                ZappBottomActionBar(onBack = state.onBack)
            }
        }
    }
}

@Suppress("UnstableCollections")
@Composable
private fun ReceiveContents(
    items: List<ReceiveAddressState>,
    modifier: Modifier = Modifier,
) {
    val c = ZappTheme.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        items.forEachIndexed { index, state ->
            if (index != 0) {
                Spacer(Modifier.height(12.dp))
            }
            AddressPanel(
                state = state,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(20.dp))

        // Swiss tip block — replaces the centered illustration + caption from
        // the old layout. Same information, lower visual volume.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .background(c.accent, RectangleShape),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "TIP",
                    style = ZappTheme.typography.eyebrow.copy(
                        color = c.textSubtle,
                        fontSize = 10.sp,
                        letterSpacing = 1.8.sp,
                        fontWeight = FontWeight.Black,
                    ),
                )
                Spacer(Modifier.height(4.dp))
                BasicText(
                    text = stringResource(id = R.string.receive_prioritize_shielded),
                    style = ZappTheme.typography.body.copy(
                        color = c.textMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    ),
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AddressPanel(
    state: ReceiveAddressState,
    modifier: Modifier = Modifier,
) {
    val c = ZappTheme.colors

    Column(
        modifier = modifier
            .wrapContentHeight()
            .background(c.surface, RectangleShape)
            .border(BorderStroke(1.dp, c.border), RectangleShape)
            .semantics(mergeDescendants = true) { role = Role.Button }
            .clickable(onClick = state.onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Image(
                    modifier = Modifier.size(40.dp),
                    painter = painterResource(id = state.icon),
                    contentDescription = null,
                )
                if (state.isShielded) {
                    Image(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .offset(1.5.dp, .5.dp),
                        painter = painterResource(
                            co.electriccoin.zcash.ui.design.R.drawable.ic_zec_shielded,
                        ),
                        contentDescription = null,
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = state.title.getValue(),
                    style = ZappTheme.typography.rowTitle.copy(
                        color = c.text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.2).sp,
                    ),
                )
                Spacer(Modifier.height(4.dp))
                BasicText(
                    text = state.subtitle.getValue(),
                    style = ZappTheme.typography.rowSubtitle.copy(
                        color = c.textMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.Monospace,
                    ),
                )
            }

            Spacer(Modifier.width(8.dp))

            ZashiImageButton(
                modifier = Modifier.size(48.dp),
                state = state.infoIconButton,
            )
        }

        AnimatedVisibility(visible = state.isExpanded) {
            Column {
                // Sharp 1dp rule between header and actions, matches the
                // section-divider rhythm used elsewhere in the Swiss surfaces.
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(c.border, RectangleShape),
                )
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.isShielded) {
                        ReceiveIconButton(
                            iconPainter = painterResource(id = R.drawable.ic_copy_shielded),
                            onClick = state.onCopyClicked,
                            text = stringResource(id = R.string.receive_copy),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    ReceiveIconButton(
                        iconPainter = painterResource(id = R.drawable.ic_qr_code_shielded),
                        onClick = state.onQrClicked,
                        text = stringResource(id = R.string.receive_qr_code),
                        modifier = Modifier.weight(1f),
                    )
                    ReceiveIconButton(
                        iconPainter = painterResource(id = R.drawable.ic_request_shielded),
                        onClick = state.onRequestClicked,
                        text = stringResource(id = R.string.receive_request),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiveIconButton(
    iconPainter: Painter,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = ZappTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(c.surfaceAlt, RectangleShape)
            .border(BorderStroke(1.dp, c.border), RectangleShape)
            .semantics {
                contentDescription = text
                role = Role.Button
            }
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
    ) {
        Image(
            painter = iconPainter,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.height(6.dp))
        BasicText(
            text = text.uppercase(),
            style = ZappTheme.typography.eyebrow.copy(
                color = c.text,
                fontSize = 10.sp,
                letterSpacing = 1.0.sp,
                fontWeight = FontWeight.Black,
            ),
        )
    }
}

@Composable
@PreviewScreens
private fun LoadingPreview() =
    ZcashTheme(forceDarkMode = true) {
        ReceiveView(
            state =
                ReceiveState(
                    items = null,
                    isLoading = true,
                    onBack = {}
                ),
            appBarState = ZashiMainTopAppBarStateFixture.new()
        )
    }

@PreviewScreens
@Composable
private fun ZashiPreview() =
    ZcashTheme {
        ReceiveView(
            state =
                ReceiveState(
                    items =
                        listOf(
                            ReceiveAddressState(
                                icon = R.drawable.ic_zec_round_full,
                                title = stringRes("Zodl"),
                                subtitle = styledStringResource("subtitle"),
                                isShielded = true,
                                onCopyClicked = {},
                                onQrClicked = { },
                                onRequestClicked = {},
                                isExpanded = true,
                                onClick = {},
                                colorMode = ReceiveAddressState.ColorMode.ZASHI,
                                infoIconButton =
                                    IconButtonState(
                                        R.drawable.ic_receive_zashi_shielded_info,
                                        onClick = {}
                                    )
                            ),
                            ReceiveAddressState(
                                icon = R.drawable.ic_zec_round_full,
                                title = stringRes("Zodl"),
                                subtitle = styledStringResource("subtitle"),
                                isShielded = false,
                                onCopyClicked = {},
                                onQrClicked = { },
                                onRequestClicked = { },
                                isExpanded = true,
                                onClick = {},
                                colorMode = ReceiveAddressState.ColorMode.DEFAULT,
                                infoIconButton =
                                    IconButtonState(
                                        R.drawable.ic_receive_zashi_shielded_info,
                                        onClick = {}
                                    )
                            )
                        ),
                    isLoading = false,
                    onBack = {}
                ),
            appBarState = ZashiMainTopAppBarStateFixture.new()
        )
    }

@PreviewScreens
@Composable
private fun KeystonePreview() =
    ZcashTheme {
        ReceiveView(
            state =
                ReceiveState(
                    items =
                        listOf(
                            ReceiveAddressState(
                                icon = co.electriccoin.zcash.ui.design.R.drawable.ic_item_keystone,
                                title = stringRes("Zodl"),
                                subtitle = styledStringResource("subtitle"),
                                isShielded = true,
                                onCopyClicked = {},
                                onQrClicked = { },
                                onRequestClicked = {},
                                isExpanded = true,
                                onClick = {},
                                colorMode = ReceiveAddressState.ColorMode.KEYSTONE,
                                infoIconButton =
                                    IconButtonState(
                                        R.drawable.ic_receive_zashi_shielded_info,
                                        onClick = {}
                                    )
                            ),
                            ReceiveAddressState(
                                icon = co.electriccoin.zcash.ui.design.R.drawable.ic_item_keystone,
                                title = stringRes("Zodl"),
                                subtitle = styledStringResource("subtitle"),
                                isShielded = false,
                                onCopyClicked = {},
                                onQrClicked = { },
                                onRequestClicked = { },
                                isExpanded = true,
                                onClick = {},
                                colorMode = ReceiveAddressState.ColorMode.DEFAULT,
                                infoIconButton =
                                    IconButtonState(
                                        R.drawable.ic_receive_zashi_shielded_info,
                                        onClick = {}
                                    )
                            )
                        ),
                    isLoading = false,
                    onBack = {}
                ),
            appBarState = ZashiMainTopAppBarStateFixture.new()
        )
    }
