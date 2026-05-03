# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Repo Is

Zapp Android — a privacy-focused Zcash wallet forked from Zodl 3.3.1, with additions:
- P2P encrypted chat via Hyperswarm (BareKit JS runtime embedded in Kotlin)
- 4-tab navigation shell (Wallet, Chats, Contacts, Settings)
- Fiat on/off-ramp via peer.xyz integration
- ZKP2P providers (PIX, UPI, GCash) for Phase C

## Required Sibling Repos

Build fails without these siblings checked out at the same level as this repo:

```
../zappMessaging/     # P2P SDK — github.com/JustZappIt/zappMessaging
../bare-kit/          # Hyperswarm JS runtime — github.com/holepunchto/bare-kit v2.0.0
```

Pinned SHAs live in `.zapp-deps`. If builds break after a pull, verify sibling revisions match.

## Build Commands

```bash
# Debug build (mainnet by default)
./gradlew :app:assembleZcashmainnetStoreDebug

# Testnet debug build
./gradlew :app:assembleZcashtestnetStoreDebug

# Install on connected device
./gradlew :app:installZcashmainnetStoreDebug

# Build with explicit network flag
./gradlew -PZCASH_NETWORK=mainnet :app:assembleDebug
```

## Test Commands

```bash
# Unit tests (Kotlin-only modules)
./gradlew check

# Android instrumentation tests (requires device/emulator)
./gradlew connectedCheck

# UI tests on specific module
./gradlew :ui-lib:connectedCheck

# Gradle Managed Device tests (no physical device needed)
./gradlew :ui-lib:pixel2TargetDebugAndroidTest
```

## Lint & Formatting

```bash
./gradlew detektAll       # Static analysis
./gradlew ktlintFormat    # Auto-format Kotlin
./gradlew lint            # Android lint
```

## Environment Requirements

- **JDK 17** (not 21+ — breaks Kotlin compiler parse). Set `JAVA_HOME` explicitly.
- **NDK 27.0.12077973** (exact version; CMake 4.1.2 also required for bare-kit native build)
- Android `compileSdk 36`, `minSdk 27`

Verify environment:
```bash
./gradlew --version  # should show Gradle 8.14.4, Launcher JVM 17.x
```

## Build Cache Issues

If you build both `zodl-android` (upstream) and this fork on the same machine, precompiled-script-plugin hash conflicts occur. Fix:

```bash
./gradlew --stop
rm -rf ~/.gradle/caches/build-cache-1 build-conventions-secant/{build,.gradle,.kotlin} .gradle
./gradlew :app:installZcashmainnetFossDebug --no-build-cache
```

## Architecture

### Module Layout

| Module | Purpose |
|--------|---------|
| `app` | Application entry, Koin DI init, crash/analytics setup |
| `ui-lib` | 99% of UI — all screens, navigation, ViewModels, DI modules |
| `ui-design-lib` | `ZappTheme` design system (tokens, components) |
| `sdk-ext-lib` | Zcash SDK extensions |
| `preference-*-lib` | Key-value storage (multiplatform API + DataStore impl) |
| `configuration-*-lib` | Remote feature-flag config (multiplatform API + Android impl) |
| `crash-*-lib` | Crash reporting (multiplatform API + Android impl) |
| `spackle-*-lib` | Common utilities |
| `build-info-lib` | Build metadata injection |

### Key Architectural Patterns

**Dependency Injection — Koin (not Hilt)**
All DI modules live in `ui-lib`. VMs are declared `viewModel { ... }`. Key modules:
- `CoreModule`, `ProviderModule`, `RepositoryModule`, `UseCaseModule`, `ViewModelModule`
- `ZappMessagingModule` wires the P2P chat SDK

**State — Kotlin Flow only (no LiveData)**
- VMs expose `StateFlow<UiState>` consumed via `.collectAsStateWithLifecycle()`
- Always use `SharingStarted.WhileSubscribed(ANDROID_STATE_FLOW_TIMEOUT)` — requires `import kotlinx.coroutines.flow.WhileSubscribed` (without this import, compiler resolves to the Long overload and fails)

**Navigation — Jetpack Compose Navigation with `@Serializable` routes**
- `RootNavGraph` → `WalletNavGraph` + `ChatNavGraph`
- `MainActivity` (unexported; exposed via activity-alias in manifest)

**Screen structure** — each screen follows:
```
screen/[name]/
  [Name]View.kt    # Composable UI
  [Name]VM.kt      # ViewModel
  [Name]State.kt   # Sealed state classes
```

**P2P Chat** — `ZappMessagingSDK` runs a Hyperswarm node via BareKit (embedded JS VM). Ed25519 identity is derived from the BIP39 seed. Never call Hyperswarm directly from UI; use `ChatViewModel` → SDK.

### UI Design System

Use `ZappTheme` tokens exclusively. Do not use `ZashiColors`, `ZashiTypography`, or rounded shapes (legacy).

- Colors: `ZappTheme.colors.*` (warm off-white background, `#FF9417` orange accent)
- Typography: `ZappTheme.typography.*`
- Components: `ZappScreenHeader`, `ZappRow`, `ZappRowDivider`, `ZappBottomActionBar`, `ZappBackButton`
- No rounded corners (Swiss-minimalist design)

`com.google.android.material` is **not** a dependency of `ui-lib`. Use `Color.parseColor("#FF9417")` or color resources — not `MaterialColors`.

### Build Variants

- **Network dimension**: `zcashmainnet` / `zcashtestnet`
- **Distribution dimension**: `store` / `foss`
- ML Kit barcode scanning only in `store` flavor (proprietary)

### Dependency Locking

Dependency locking is enabled. After changing `gradle.properties` or adding dependencies, update lock files:
```bash
./gradlew resolveAndLockAll --write-locks
```

### Suspend Navigation Use Cases

`NavigateToPeerOnrampUseCase` and similar use-case classes are suspend functions. Always call them inside a coroutine scope:
```kotlin
viewModelScope.launch { navigateToPeerOnramp() }
```

## Key Files

| File | Purpose |
|------|---------|
| `app/src/main/.../ZcashApplication.kt` | Root Application — Koin init |
| `ui-lib/src/main/.../MainActivity.kt` | Main Activity |
| `ui-lib/src/main/.../RootNavGraph.kt` | Top-level navigation |
| `ui-lib/src/main/.../WalletNavGraph.kt` | Wallet tab + nested screens |
| `ui-lib/src/main/.../screen/tabs/` | 4-tab shell |
| `ZAPP_CHANGES.md` | Patch series stacked on Zodl 3.3.1 — read before merging upstream |
| `DEVELOPER_SETUP.md` | Full environment setup with exact version pins |
| `docs/Architecture.md` | Module dependency rules |
