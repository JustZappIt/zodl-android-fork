# Zapp-specific Changes on top of Zodl 3.3.1

This file documents the 8 commits that Zapp stacks on top of Zodl commit
`2409c4d7` (v3.3.1, tagged `upstream/zodl-3.3.1`). Use this as a merge guide
when pulling upstream Zodl updates.

---

## Patch series (oldest → newest)

### 1. `8b3b1566` — Fork zodl with ZappMessaging P2P chat integration

Adds ZappMessaging as a Gradle composite build sibling
(`../zappMessaging/android` and `../bare-kit/android`), wires the ZappMessaging
SDK into the DI graph, introduces `ChatViewModel` and `ChatListView`,
and adds a Chats tab and a Contacts tab to the bottom nav shell alongside the
existing Zodl Wallet tab.

**High-conflict files**: `settings.gradle.kts`, `app/build.gradle.kts`,
`ui-lib/.../WalletNavGraph.kt`, `ui-lib/.../RootNavGraph.kt`,
`ui-lib/.../di/`

---

### 2. `d6447193` — Add back/contacts navigation to chat list

Wires the back-navigation affordance in `ChatListView` and a contact-picker
navigation path so the chat list can push into contacts and pop back.

**High-conflict files**: `ui-lib/.../WalletNavGraph.kt`,
`ui-lib/.../ui/screen/chat/`

---

### 3. `e3525517` — Add Wallet tab and rewire onboarding into the tabs shell

- Renames the Pay tab to Wallet; `WalletTabContent` shows a setup screen when
  no wallet exists, a spinner while loading, and embeds `AndroidHome` (balance
  + send/receive/pay/swap) once ready.
- `RootNavGraph` now starts at `MainAppGraph` (tabs shell always visible);
  removes `SecretState`-driven graph switching; registers `Restore*`
  composables inside `walletNavGraph`.
- Removes the now-unused `OnboardingNavGraph` and `OnboardingNavigation`.
- Removes the Chat shortcut button from `HomeVM` (chat is in bottom nav).
- Stubs `ChatViewModel.updateDisplayName` to a local-only state update.

**High-conflict files**: `ui-lib/.../RootNavGraph.kt`,
`ui-lib/.../WalletNavGraph.kt`, `ui-lib/.../ui/screen/tabs/view/`,
`ui-lib/.../ui/screen/home/`

---

### 4. `c9ff123e` — Add chat attachments, network status pill, and scan-to-add contact

- Scan-to-add contact: QR scan survives the scanner navigation round-trip;
  trigger moved to `ChatViewModel` so `viewModelScope` outlives the caller's
  disposed composition.
- Attachment sheets matching Zapp design: outer `AttachmentSheet` with Share
  Address / Send ZEC / Attach Media, inner `MediaAttachmentSheet` with
  Media / File / Camera / Location.
- Message bubbles for Media, File, Location, WalletAddress, PaymentRequest, and
  Transaction types.
- Network status pill in `ChatListView` and `ChatRoomView` top bars; tap opens
  `NetworkDetailsSheet`.
- Deps added: `coil-compose`, `play-services-location`.

**High-conflict files**: `ui-lib/.../ui/screen/chat/`,
`ui-lib/src/main/AndroidManifest.xml`

---

### 5. `5b4fdd84` — Document exact build environment in README

Adds a "Build Environment" section to `README.md` listing pinned JDK, Gradle,
AGP, Kotlin, Android SDK/NDK/CMake versions, and sibling repo layout.

**High-conflict files**: `README.md` (low risk)

---

### 6. `0922573f` — Rebrand to Zapp and add wallet settings card

- Renames the account-switcher label from "Zodl" to "Zapp".
- Gates the Settings Wallet card on an existing wallet (seed backup, server,
  and advanced shortcuts).
- Drops the Keystone hardware-wallet promo from the account list.
- Adds `ZappPalette` color tokens to `ui-design-lib`.

**High-conflict files**: `ui-lib/.../ui/screen/tabs/view/SettingsTabContent.kt`,
`ui-design-lib/.../ZappPalette.kt`, `ui-lib/.../ui/screen/account/`

---

### 7. `095c4c3e` — Enable testnet and consolidate the network toggle

- Drives `R.bool.zcash_is_testnet` from the `network` product flavor via a
  generated `resValue` in `app/build.gradle.kts`, preventing drift from
  `BuildConfig.FLAVOR_network`.
- Fixes a latent bug where Internal testnet variants had no `bools.xml` overlay
  and silently ran on mainnet.
- Adds optional `ZCASH_NETWORK=mainnet|testnet` property to filter variants.

**High-conflict files**: `app/build.gradle.kts`, `gradle.properties`

---

### 8. `19543543` — Add balance-history chart to the Wallet home

- Introduces `SparkChart` Compose component in `ui-design-lib` (Canvas stroke +
  vertical gradient fill, data-agnostic).
- Adds `BalanceChartWidget` on the Wallet tab with 24h/1w/1m/All period chips.
- New `GetBalanceHistoryUseCase` produces a running signed-delta series from
  `TransactionRepository` — no backend required; works on mainnet and testnet.
- Chart hides entirely on a fresh wallet (no transactions); shows
  "not enough activity" for empty periods.

**High-conflict files**: `ui-lib/.../ui/screen/tabs/view/WalletTabContent.kt`,
`ui-design-lib/.../`, `ui-lib/.../di/UseCaseModule.kt`

---

## High-conflict files summary (for upstream merges)

When merging a new Zodl release, expect conflicts in:

| File | Reason |
|------|--------|
| `settings.gradle.kts` | Zapp adds `zappmessaging` + `bare-kit` composite includes |
| `gradle.properties` | Zapp sets `ZCASH_RELEASE_APP_NAME=Zapp`, `ZCASH_VERSION_NAME=4.x.x` |
| `app/build.gradle.kts` | Zapp adds testnet `resValue` and variant logic |
| `ui-lib/.../WalletNavGraph.kt` | Zapp adds chat/contacts/scan routes |
| `ui-lib/.../RootNavGraph.kt` | Zapp changes start destination to tabs shell |
| `ui-lib/.../ui/screen/tabs/view/` | All tab content composables are Zapp-authored |
| `ui-design-lib/.../ZappPalette.kt` | Zapp-specific color tokens |

## Merge procedure

```bash
# 1. Fetch new Zodl onto the tracking branch
git fetch <zodl-remote> main:upstream/zodl

# 2. Create a merge branch
git checkout -b zapp/merge-zodl-X.Y.Z main

# 3. Merge — conflicts will be in the files above
git merge upstream/zodl

# 4. Resolve, verify composite build, open PR into main
./gradlew :app:assembleZcashmainnetStoreDebug
```
