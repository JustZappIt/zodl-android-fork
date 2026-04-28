package co.electriccoin.zcash.ui.screen.advancedsettings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ripple
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.electriccoin.zcash.ui.R
import co.electriccoin.zcash.ui.design.component.ButtonState
import co.electriccoin.zcash.ui.design.component.listitem.ListItemState
import co.electriccoin.zcash.ui.design.component.zapp.ZappBackButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappButton
import co.electriccoin.zcash.ui.design.component.zapp.ZappButtonVariant
import co.electriccoin.zcash.ui.design.component.zapp.ZappRowDivider
import co.electriccoin.zcash.ui.design.component.zapp.ZappScreenHeader
import co.electriccoin.zcash.ui.design.newcomponent.PreviewScreens
import co.electriccoin.zcash.ui.design.theme.ZappTheme
import co.electriccoin.zcash.ui.design.theme.ZcashTheme
import co.electriccoin.zcash.ui.design.util.getValue
import co.electriccoin.zcash.ui.design.util.imageRes
import co.electriccoin.zcash.ui.design.util.stringRes
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AdvancedSettings(
    state: AdvancedSettingsState,
) {
    val c = ZappTheme.colors
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = c.bg,
        topBar = {
            ZappScreenHeader(
                title = stringResource(id = R.string.advanced_settings_title),
                modifier = Modifier.testTag(AdvancedSettingsTag.ADVANCED_SETTINGS_TOP_APP_BAR),
                left = { ZappBackButton(onClick = state.onBack) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            // Card-style bordered surface enclosing the list (Zapp settings convention)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .background(c.surface, RectangleShape)
                    .border(BorderStroke(1.dp, c.border), RectangleShape),
            ) {
                state.items.forEachIndexed { index, item ->
                    AdvancedSettingsRow(item)
                    if (index != state.items.lastIndex) {
                        ZappRowDivider(inset = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Info()

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                ZappButton(
                    text = state.deleteButton.text.getValue(),
                    onClick = state.deleteButton.onClick,
                    enabled = state.deleteButton.isEnabled,
                    variant = ZappButtonVariant.Danger,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AdvancedSettingsRow(item: ListItemState) {
    val c = ZappTheme.colors
    val onClick = item.onClick
    val baseModifier = Modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 56.dp)

    val rowModifier = if (onClick != null && item.isEnabled) {
        baseModifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = c.accent),
            onClick = onClick,
        )
    } else {
        baseModifier
    }

    Row(
        modifier = rowModifier.padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item.bigIcon?.let { resource ->
            ResourceIcon(resource = resource)
        }

        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = item.title.getValue(),
                style = ZappTheme.typography.rowTitle.copy(
                    color = if (item.isEnabled) c.text else c.textSubtle,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            item.subtitle?.let { sub ->
                Spacer(Modifier.height(2.dp))
                BasicText(
                    text = sub.getValue(),
                    style = ZappTheme.typography.caption.copy(
                        color = if (item.isEnabled) c.textMuted else c.textSubtle,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (onClick != null && item.isEnabled) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = c.textSubtle,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// Zapp Advanced Settings uses brand yellow as its primary accent —
// distinct from the global app accent (orange) so this section reads as
// "advanced / utility" without breaking the rest of the design language.
private val AdvancedYellow = Color(0xFFFCBB1A)
private val AdvancedYellowSoft = Color(0xFFFFF2C6)
private val AdvancedYellowText = Color(0xFF6B4F00)

@Composable
private fun ResourceIcon(resource: co.electriccoin.zcash.ui.design.util.ImageResource) {
    val drawableId = (resource as? co.electriccoin.zcash.ui.design.util.ImageResource.ByDrawable)?.resource
    if (drawableId != null) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(AdvancedYellowSoft, RectangleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(AdvancedYellowText),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun Info() {
    val c = ZappTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_advanced_settings_info),
            contentDescription = null,
            colorFilter = ColorFilter.tint(c.textMuted),
        )
        Spacer(modifier = Modifier.size(12.dp))
        BasicText(
            text = stringResource(id = R.string.advanced_settings_info),
            style = ZappTheme.typography.caption.copy(color = c.textMuted),
        )
    }
}

@PreviewScreens
@Composable
private fun AdvancedSettingsPreview() =
    ZcashTheme {
        AdvancedSettings(
            state =
                AdvancedSettingsState(
                    onBack = {},
                    items =
                        persistentListOf(
                            ListItemState(
                                title = stringRes(R.string.advanced_settings_recovery),
                                bigIcon = imageRes(R.drawable.ic_advanced_settings_recovery),
                                onClick = {}
                            ),
                            ListItemState(
                                title = stringRes(R.string.advanced_settings_export),
                                bigIcon = imageRes(R.drawable.ic_advanced_settings_export),
                                onClick = {}
                            ),
                        ),
                    deleteButton =
                        ButtonState(
                            text = stringRes(R.string.advanced_settings_delete_button),
                            onClick = {}
                        )
                ),
        )
    }
