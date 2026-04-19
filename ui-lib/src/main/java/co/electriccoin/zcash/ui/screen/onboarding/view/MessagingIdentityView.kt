@file:Suppress("TooManyFunctions")

package co.electriccoin.zcash.ui.screen.onboarding.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.electriccoin.zcash.ui.design.component.ZashiButton
import co.electriccoin.zcash.ui.design.component.ZashiButtonDefaults
import co.electriccoin.zcash.ui.design.component.ZashiTopAppBarBackNavigation
import co.electriccoin.zcash.ui.design.newcomponent.PreviewScreens
import co.electriccoin.zcash.ui.design.theme.ZcashTheme
import co.electriccoin.zcash.ui.design.theme.colors.ZappPalette
import co.electriccoin.zcash.ui.design.theme.colors.ZashiColors
import co.electriccoin.zcash.ui.design.theme.typography.ZashiTypography

private val MSG_SEED_WORDS = listOf(
    "river", "anchor", "willow", "lantern", "silver", "harbor",
    "maple", "breeze", "copper", "quartz", "canyon", "ember"
)

// ───────────────────────────────────────────────
// Progress bar — reused across all onboarding steps
// ───────────────────────────────────────────────

@Composable
fun OnboardingProgressBar(
    step: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        repeat(total) { i ->
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(
                            color = if (i < step) ZappPalette.Primary else ZashiColors.Surfaces.strokePrimary,
                        ),
            )
        }
    }
}

// ───────────────────────────────────────────────
// Step 1/6 — Messaging phase intro
// ───────────────────────────────────────────────

@Composable
fun MessagingPhaseIntro(
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    Scaffold { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(16.dp))

            OnboardingProgressBar(step = 1, total = 6)

            Spacer(Modifier.height(24.dp))

            ZashiTopAppBarBackNavigation(onBack = onBack)

            Spacer(Modifier.height(24.dp))

            // Phase badge
            Text(
                text = "PART 1 OF 3 · MESSAGING",
                color = ZappPalette.Primary,
                style = ZashiTypography.textXs,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Create your messaging identity",
                style = ZashiTypography.header6,
                fontWeight = FontWeight.Bold,
                color = ZashiColors.Text.textPrimary,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Your messaging identity is separate from your wallet. It controls how others find you and how your messages are signed.",
                style = ZashiTypography.textSm,
                color = ZashiColors.Text.textTertiary,
            )

            Spacer(Modifier.height(24.dp))

            BulletRow(
                icon = "⊕",
                title = "Username",
                detail = "A human-readable handle (e.g. @alice) backed by an Ed25519 keypair.",
            )

            Spacer(Modifier.height(16.dp))

            BulletRow(
                icon = "⊕",
                title = "Recovery phrase",
                detail = "12 words that let you restore your messaging identity on a new device.",
            )

            Spacer(Modifier.weight(1f))

            ZashiButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Continue",
                onClick = onContinue,
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ───────────────────────────────────────────────
// Step 2/6 — Username entry
// ───────────────────────────────────────────────

@Composable
fun UsernameEntryScreen(
    onBack: () -> Unit,
    onContinue: (username: String) -> Unit,
) {
    var username by rememberSaveable { mutableStateOf("") }

    val isLongEnough = username.length >= 3
    val isShortEnough = username.length <= 20
    val isValidChars = username.matches(Regex("[a-z0-9_]*"))
    val isValid = isLongEnough && isShortEnough && isValidChars && username.isNotEmpty()

    Scaffold { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(16.dp))

            OnboardingProgressBar(step = 2, total = 6)

            Spacer(Modifier.height(24.dp))

            ZashiTopAppBarBackNavigation(onBack = onBack)

            Spacer(Modifier.height(24.dp))

            Text(
                text = "PART 1 · USERNAME",
                color = ZappPalette.Primary,
                style = ZashiTypography.textXs,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Pick a username",
                style = ZashiTypography.header6,
                fontWeight = FontWeight.Bold,
                color = ZashiColors.Text.textPrimary,
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { new ->
                    if (new.length <= 20) username = new.lowercase()
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("e.g. alice", color = ZashiColors.Text.textTertiary)
                },
                prefix = {
                    Text("@", color = ZashiColors.Text.textTertiary)
                },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done,
                    ),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZappPalette.Primary,
                        unfocusedBorderColor = ZashiColors.Surfaces.strokePrimary,
                        focusedTextColor = ZashiColors.Text.textPrimary,
                        unfocusedTextColor = ZashiColors.Text.textPrimary,
                        cursorColor = ZappPalette.Primary,
                    ),
                shape = RoundedCornerShape(0.dp),
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValidationChip(label = "3+ chars", passed = isLongEnough && username.isNotEmpty())
                ValidationChip(label = "≤20 chars", passed = isShortEnough)
                ValidationChip(label = "a–z, 0–9, _", passed = isValidChars && username.isNotEmpty())
            }

            Spacer(Modifier.height(20.dp))

            // Info box
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(ZashiColors.Surfaces.bgSecondary)
                        .padding(16.dp),
            ) {
                Text(
                    text = "About your messaging identity",
                    style = ZashiTypography.textSm,
                    fontWeight = FontWeight.SemiBold,
                    color = ZashiColors.Text.textPrimary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Your username is paired with an Ed25519 keypair generated on this device. Others discover you by username, but messages are authenticated by your key — not just the name.",
                    style = ZashiTypography.textXs,
                    color = ZashiColors.Text.textTertiary,
                )
            }

            Spacer(Modifier.weight(1f))

            ZashiButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Continue",
                onClick = { if (isValid) onContinue(username) },
                colors = if (isValid) ZashiButtonDefaults.primaryColors() else ZashiButtonDefaults.tertiaryColors(),
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ───────────────────────────────────────────────
// Step 3/6 — Messaging seed phrase
// ───────────────────────────────────────────────

@Composable
fun MessagingSeedPhraseScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    var isRevealed by rememberSaveable { mutableStateOf(false) }
    var isConfirmed by rememberSaveable { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(16.dp))

            OnboardingProgressBar(step = 3, total = 6)

            Spacer(Modifier.height(24.dp))

            ZashiTopAppBarBackNavigation(onBack = onBack)

            Spacer(Modifier.height(24.dp))

            Text(
                text = "PART 1 · RECOVERY PHRASE",
                color = ZappPalette.Primary,
                style = ZashiTypography.textXs,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Messaging recovery phrase",
                style = ZashiTypography.header6,
                fontWeight = FontWeight.Bold,
                color = ZashiColors.Text.textPrimary,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Write down these 12 words in order. You'll need them to restore your messaging identity.",
                style = ZashiTypography.textSm,
                color = ZashiColors.Text.textTertiary,
            )

            Spacer(Modifier.height(24.dp))

            // 12-word grid
            Box {
                SeedWordGrid(
                    words = MSG_SEED_WORDS,
                    modifier = Modifier.blur(if (isRevealed) 0.dp else 14.dp),
                )

                if (!isRevealed) {
                    Box(
                        modifier =
                            Modifier
                                .matchParentSize()
                                .clickable { isRevealed = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Tap to reveal",
                            style = ZashiTypography.textSm,
                            fontWeight = FontWeight.SemiBold,
                            color = ZashiColors.Text.textPrimary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { if (isRevealed) isConfirmed = !isConfirmed }
                        .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = isConfirmed,
                    onCheckedChange = { if (isRevealed) isConfirmed = it },
                    colors =
                        CheckboxDefaults.colors(
                            checkedColor = ZappPalette.Primary,
                            uncheckedColor = ZashiColors.Surfaces.strokePrimary,
                        ),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "I've written down all 12 words",
                    style = ZashiTypography.textSm,
                    color = ZashiColors.Text.textPrimary,
                )
            }

            Spacer(Modifier.weight(1f))

            ZashiButton(
                modifier = Modifier.fillMaxWidth(),
                text = "I've saved it",
                onClick = { if (isRevealed && isConfirmed) onContinue() },
                colors =
                    if (isRevealed && isConfirmed) {
                        ZashiButtonDefaults.primaryColors()
                    } else {
                        ZashiButtonDefaults.tertiaryColors()
                    },
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ───────────────────────────────────────────────
// Internal helpers
// ───────────────────────────────────────────────

@Composable
private fun SeedWordGrid(
    words: List<String>,
    modifier: Modifier = Modifier,
) {
    val half = words.size / 2
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            words.take(half).forEachIndexed { i, word ->
                SeedWordItem(index = i + 1, word = word)
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            words.drop(half).forEachIndexed { i, word ->
                SeedWordItem(index = i + half + 1, word = word)
            }
        }
    }
}

@Composable
private fun SeedWordItem(
    index: Int,
    word: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(ZashiColors.Surfaces.bgSecondary)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$index",
            style = ZashiTypography.textXs,
            color = ZashiColors.Text.textTertiary,
            modifier = Modifier.width(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = word,
            style = ZashiTypography.textSm,
            fontWeight = FontWeight.Medium,
            color = ZashiColors.Text.textPrimary,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun BulletRow(
    icon: String,
    title: String,
    detail: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(ZashiColors.Surfaces.bgSecondary)
                .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = icon,
            color = ZappPalette.Primary,
            style = ZashiTypography.textSm,
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = ZashiTypography.textSm,
                fontWeight = FontWeight.SemiBold,
                color = ZashiColors.Text.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = detail,
                style = ZashiTypography.textXs,
                color = ZashiColors.Text.textTertiary,
            )
        }
    }
}

@Composable
private fun ValidationChip(
    label: String,
    passed: Boolean,
) {
    val bg = if (passed) ZappPalette.Primary.copy(alpha = 0.1f) else ZashiColors.Surfaces.bgSecondary
    val fg = if (passed) ZappPalette.Primary else ZashiColors.Text.textTertiary

    Text(
        text = label,
        style = ZashiTypography.textXs,
        color = fg,
        modifier =
            Modifier
                .background(bg)
                .border(
                    width = 1.dp,
                    color = if (passed) ZappPalette.Primary else ZashiColors.Surfaces.strokePrimary,
                    shape = RoundedCornerShape(0.dp),
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

// ───────────────────────────────────────────────
// Previews
// ───────────────────────────────────────────────

@PreviewScreens
@Composable
private fun MessagingPhaseIntroPreview() =
    ZcashTheme {
        MessagingPhaseIntro(onBack = {}, onContinue = {})
    }

@PreviewScreens
@Composable
private fun UsernameEntryPreview() =
    ZcashTheme {
        UsernameEntryScreen(onBack = {}, onContinue = {})
    }

@PreviewScreens
@Composable
private fun MessagingSeedPreview() =
    ZcashTheme {
        MessagingSeedPhraseScreen(onBack = {}, onContinue = {})
    }
