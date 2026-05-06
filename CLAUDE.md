# CLAUDE.md

Zapp Android — privacy-focused Zcash wallet (Zodl 3.3.1 fork) with P2P chat (Hyperswarm/BareKit), 4-tab shell, fiat on/off-ramp (peer.xyz), ZKP2P providers.

## Sibling repos (required at same level)

```
../zappMessaging/   # P2P SDK
../bare-kit/        # Hyperswarm JS runtime v2.0.0
```

Pinned SHAs in `.zapp-deps`. Match them if builds break after a pull.

## Build

```bash
./gradlew :app:assembleZcashtestnetStoreDebug    # debug (default network = testnet)
./gradlew :app:installZcashtestnetStoreDebug     # install on device
./gradlew check                                  # unit tests
./gradlew detektAll && ./gradlew ktlintFormat    # lint + format
./gradlew resolveAndLockAll --write-locks        # after dep changes
```

`gradle.properties` pins `ZCASH_NETWORK=testnet` so only `Zcashtestnet*` Gradle
tasks are registered. To target mainnet for a build, override at invocation:

```bash
./gradlew -PZCASH_NETWORK=mainnet :app:installZcashmainnetStoreDebug
```

Build cache conflict (when building both zodl-android + this fork):
```bash
./gradlew --stop && rm -rf ~/.gradle/caches/build-cache-1 build-conventions-secant/{build,.gradle,.kotlin} .gradle
```

## Environment

- JDK 17 (not 21+), NDK 27.0.12077973, CMake 4.1.2
- `compileSdk 36`, `minSdk 27`, Gradle 8.14.4

## Architecture

| Module | Purpose |
|--------|---------|
| `app` | Koin DI init, crash/analytics |
| `ui-lib` | All screens, ViewModels, nav, DI modules |
| `ui-design-lib` | `ZappTheme` — tokens, components |
| `sdk-ext-lib` | Zcash SDK extensions |
| `preference-*-lib` | DataStore key-value storage |
| `configuration-*-lib` | Remote feature flags |

**DI** — Koin (`viewModel { ... }`), not Hilt. Modules in `ui-lib`.

**State** — `StateFlow<UiState>` + `.collectAsStateWithLifecycle()`. Always `SharingStarted.WhileSubscribed(ANDROID_STATE_FLOW_TIMEOUT)` with explicit `import kotlinx.coroutines.flow.WhileSubscribed`.

**Navigation** — `@Serializable` routes. Top-level: `RootNavGraph` → `WalletNavGraph`. Chat sub-graph (`ChatNavGraph.kt`) is registered inside `MainAppGraph` via `chatNavGraph(navigationRouter)` for back-stack continuity with the tabs shell.

**Screen layout** — `screen/[name]/[Name]View.kt`, `[Name]VM.kt`, `[Name]State.kt`.

**P2P Chat** — `ZappMessagingSDK` via BareKit. Never call Hyperswarm from UI; go through `ChatViewModel`.

**UI design system** — `ZappTheme` tokens only. No `ZashiColors`, no `ZashiTypography`, no rounded shapes. See `zapp-android-ui` skill.

**Build variants** — network: `zcashmainnet`/`zcashtestnet` × distribution: `store`/`foss`. The active network is selected by `ZCASH_NETWORK` in `gradle.properties` (currently `testnet`); the other network's variants are disabled at the AGP `beforeVariants` level. ML Kit barcode only in `store`.

**Suspend nav use cases** — always `viewModelScope.launch { navigateTo...() }`.

**Hidden upstream features** — features carried over from upstream Zashi but not
exposed in the fork's UI are kept as commented blocks prefixed
`// DEAD CODE [hidden]:` (Settings/Advanced/About/Backup/Tor/Tax export, etc.).
Restore by uncommenting; do not delete — keeps merges with upstream simple.

## Key files

| File | Purpose |
|------|---------|
| `app/.../ZcashApplication.kt` | Koin init |
| `ui-lib/.../MainActivity.kt` | Main activity |
| `ui-lib/.../RootNavGraph.kt` | Top-level nav |
| `ui-lib/.../WalletNavGraph.kt` | Wallet tab + nested screens |
| `ui-lib/.../ChatNavGraph.kt` | P2P chat sub-graph (called from WalletNavGraph) |
| `ui-lib/.../screen/tabs/` | 4-tab shell |
| `ZAPP_CHANGES.md` | Patch series — read before merging upstream |
| `DEVELOPER_SETUP.md` | Full env setup with version pins |
