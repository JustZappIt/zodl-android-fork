# Zapp Android Wallet

Zapp is a privacy-focused Android wallet forked from [Zodl](https://github.com/zodl-inc/zodl-android)
(v3.3.1, tag `upstream/zodl-3.3.1`), itself a fork of Electric Coin Company's
[Zashi](https://github.com/Electric-Coin-Company/zashi-android). On top of the
upstream Zcash wallet it adds:

- ZappMessaging — end-to-end encrypted P2P chat over Hyperswarm
- 4-tab navigation shell (Wallet · Chats · Contacts · Settings)
- Balance history chart on the Wallet home
- Testnet toggle consolidated into the build flavor
- Zapp rebrand (`ZappPalette`, app name, version 4.x)
- Swiss-minimalist UI system (`ZappTheme`) with full design-token migration across all screens

## UI Design System (`feat/newUI`)

A new Swiss-minimalist design system (`ZappTheme`) has been introduced and applied across all screens. Key changes:

### Design Standards
- **Colors** — all screens now use `ZappTheme.colors.*` tokens (warm off-white backgrounds, `#FF9417` orange accent). Legacy `ZashiColors`, `ZcashTheme.colors`, and raw `MaterialTheme.colorScheme` references have been replaced.
- **Typography** — all screens use `ZappTheme.typography.*` tokens. `ZashiTypography`, `RobotoMonoFontFamily`, and `MaterialTheme.typography` references have been replaced.
- **Shapes** — no rounded corners anywhere. All `RoundedCornerShape` and `CircleShape` usage has been replaced with `RectangleShape` (flat Swiss geometry).
- **Back button placement** — back buttons have moved from the top-left app bar to a `ZappBottomActionBar` at the bottom-left, horizontally aligned with the primary CTA so the thumb can reach both without stretching.

### New Component: `ZappBottomActionBar`
Added to `ui-design-lib/.../component/zapp/ZappComponents.kt`. Renders `ZappBackButton` on the left and an optional primary action composable on the right, with `windowInsetsPadding(WindowInsets.navigationBars)` for edge-to-edge correctness.

### Migrated Screens
| Screen | Changes |
|--------|---------|
| `ChatRoomView` | Replaced `MaterialTheme` + Material3 `TopAppBar` with `ZappScreenHeader` + `ZappBottomActionBar`; message bubbles use `RectangleShape` |
| `ChatProfileView` | Replaced `MaterialTheme`, `Card`, `CircleShape` with Zapp tokens; square avatar with accent background |
| `ChatSettingsView` | Replaced `MaterialTheme`, `Surface`, `HorizontalDivider` with `ZappGroupHeader`/`ZappRow`/`ZappRowDivider` |
| `ContactEditView` | Replaced Material3 `TopAppBar` + `Button` with `ZappScreenHeader` + `ZappBottomActionBar` |
| `NewConversationView` | Replaced Material3 scaffold with `ZappScreenHeader` + `ZappBottomActionBar`; contact chips use Zapp surface tokens |
| `ChatListView` | Back button moved from `ZappScreenHeader.left` to bottom-left (aligned with FAB) |
| `ChatContactsView` | Same as `ChatListView` |
| `MoreView` | Replaced `ZashiSmallTopAppBar` with `ZappScreenHeader`; `ZappBottomActionBar` added |
| `AddressBookView` | Back button moved to `ZappBottomActionBar` |
| `SendView` | Replaced `ZashiTopAppbar`/`BlankBgScaffold` with `ZappScreenHeader`+`ZappBottomActionBar`; all `ZashiColors`/`ZashiTypography`/`RobotoMonoFontFamily`/`ZashiDimensions` replaced with Zapp tokens |
| `ReceiveView` | Replaced `ZashiTopAppbar`/`BlankBgScaffold`; address panel color-mode theming replaced with flat Zapp surface tokens; `RoundedCornerShape` → `RectangleShape` |
| `SwapView` | Replaced `ZashiSmallTopAppBar`+back with `ZappScreenHeader`+`ZappBottomActionBar`; all color/typography tokens migrated |
| `TransactionDetailView` | Replaced `GradientBgScaffold`+`ZashiSmallTopAppBar` with flat `Scaffold`+`ZappScreenHeader`; `ZappBackButton` added to bottom action row |
| `ScanView` | Removed `ScanTopAppBar`; back button moved into `ScanBottomItems` using `ZappBackButton` |
| `RequestView` | Replaced `RequestTopAppBar`+`OldZashiBottomBar` with `ZappScreenHeader`+`ZappBottomActionBar` |
| `HomeView` | Legacy spacing tokens (`ZashiDimensions`, `ZcashTheme.dimens`) replaced with direct dp values |

See [`ZAPP_CHANGES.md`](ZAPP_CHANGES.md) for the full patch series and
[`ZAPP_CHANGES.md#merge-procedure`](ZAPP_CHANGES.md#merge-procedure) for how
to pull in future Zodl upstream releases.

# Reporting an issue

File a GitHub issue in this repository for Zapp-specific bugs.

For upstream Zcash/Zodl issues, please use:
 * [Zodl issues](https://github.com/zodl-inc/zodl-android/issues/new/choose)
 * [Zcash Forum](https://forum.zcashcommunity.com/)

If you wish to report a security issue, please follow the upstream
[Responsible Disclosure guidelines](https://github.com/zodl-inc/zodl-project/blob/master/responsible_disclosure.md).

# Contributing

Contributions are very much welcomed!  Please read our 
[Contributing Guidelines](docs/CONTRIBUTING.md) to learn about our process.

Branch naming: `zapp/feature/<name>`, `zapp/fix/<name>`, `chore/<description>`.
All PRs target `zapp/develop`; `main` is always-shippable.

# Getting Started

If you'd like to compile this application from source, please see our 
[Setup Documentation](docs/Setup.md) to get started.

# Build Environment — exact versions

Most build problems on a fresh machine come down to a mismatched JDK, Android
SDK component, or the sibling `../bare-kit` / `../zappMessaging` repos not
being checked out next to `zodl-android`. This section lists the exact versions
known to build this branch cleanly.

## Host

| Tool | Version | Notes |
| --- | --- | --- |
| macOS | 26.3.1 (Darwin 25.3.0, `arm64`, Apple Silicon) | Linux and Windows are supported by upstream, but not verified on this branch. |
| Android Studio | **Narwhal 2025.3.2** — build `AI-253.30387.90.2532.14935130` | Any recent Android Studio on the Narwhal (`AI-253`) channel should work; older channels have not been tested. |

## JDK

| Tool | Version | How it's selected |
| --- | --- | --- |
| JDK | **17** — any OpenJDK 17 LTS build works (Temurin and Azul Zulu are both verified) | `gradle/daemon-jvm.properties` pins the Gradle daemon to JDK 17 automatically; Gradle will auto-provision one if not present. Point `JAVA_HOME` at a JDK 17 install for first-launch `./gradlew` commands on machines where auto-provisioning hasn't run yet. JDK 18+ causes a Kotlin compiler parse error on startup — if you see `IllegalArgumentException: <number>`, your `JAVA_HOME` is wrong. |
| Android JVM target | **1.8** (`ANDROID_JVM_TARGET=1.8`) | Android doesn't support bytecode targets beyond Java 8; don't change this. |

If you see `Failed to read key AndroidDebugKey from store ...debug.keystore` or
`Algorithm HmacPBESHA256 not available`, your `JAVA_HOME` is pointing at a JDK
older than 17 — upgrade or delete `~/.android/debug.keystore` and let it
regenerate.

## Build system

| Tool | Version | Notes |
| --- | --- | --- |
| Gradle | **8.14.4** | Pinned by the wrapper (`gradle/wrapper/gradle-wrapper.properties`); never invoke a system-installed `gradle`, always use `./gradlew`. |
| Android Gradle Plugin (AGP) | **8.13.2** (`ANDROID_GRADLE_PLUGIN_VERSION`) | |
| Kotlin | **2.3.10** (`KOTLIN_VERSION`) | |
| Kotlin Compose Compiler | **1.5.15** (`ANDROIDX_COMPOSE_COMPILER_VERSION`) | |
| Detekt | 1.23.8 | |
| ktlint | 1.8.0 | |

## Android SDK components

Install these via Android Studio's **SDK Manager** (Preferences → Languages &
Frameworks → Android SDK). The versions below are exactly what `gradle.properties`
expects; if a different version is installed, Gradle will try to download the
correct one on first build.

| Component | Version |
| --- | --- |
| `compileSdk` / `targetSdk` | **36** |
| `minSdk` | **27** |
| Build Tools | **36.1.0** |
| Platform Tools (`adb`) | 36.0.0 (ADB protocol 1.0.41) |
| Android NDK | **27.0.12077973** (exact — required by `bare-kit`'s native code) |
| CMake | **4.1.2** (installed under `$ANDROID_HOME/cmake/`) |

The NDK and CMake versions are not negotiable — `bare-kit` is built with
CMake against a specific NDK ABI. Newer NDK versions (27.1.x, 28.x) will be
detected but Gradle will download the pinned 27.0.12077973 alongside them.

## Sibling repositories (composite build)

`zodl-android`'s `settings.gradle.kts` includes **two sibling projects that
must be checked out next to this repo on disk**:

```
<some-dir>/
  zodl-android/        <-- this repo
  zappMessaging/       <-- required
  bare-kit/            <-- required
```

If the directory layout differs, Gradle fails with a "project directory does
not exist" error before any Kotlin compilation happens. Clone both:

```sh
# P2P messaging SDK (JustZappIt org)
git clone https://github.com/JustZappIt/zappMessaging.git  ../zappMessaging

# Native P2P transport — upstream Holepunch library, used unmodified
# Pin to the same commit recorded in .zapp-deps (currently v2.0.0)
git clone --branch v2.0.0 https://github.com/holepunchto/bare-kit.git ../bare-kit
```

Pinned commit SHAs for both siblings are in [`.zapp-deps`](.zapp-deps).
CI verifies the checked-out SHAs match before building.

## Key runtime libraries

Pinned in `gradle.properties` and reused via the version catalog in
`settings.gradle.kts`. You don't install these — Gradle does — but when a
dependency resolution error blames one of them, these are the versions in
play:

| Area | Library | Version |
| --- | --- | --- |
| Compose | `androidx.compose.ui / foundation / material3` | UI/Foundation **1.10.4**, Material3 **1.3.1**, Icons **1.7.8** |
| AndroidX | `activity`, `lifecycle`, `navigation-compose`, `fragment` | Activity **1.12.4**, Lifecycle **2.10.0**, Navigation **2.9.7**, Fragment **1.8.9** |
| AndroidX | `core-ktx`, `splashscreen`, `work-runtime`, `browser` | Core **1.17.0**, Splash **1.2.0**, Work **2.11.1**, Browser **1.9.0** |
| AndroidX | `camera-camera2 / lifecycle / view` | **1.5.3** |
| AndroidX | `biometric / biometric-ktx` | **1.4.0-alpha05** |
| AndroidX | `security-crypto` | **1.1.0** |
| DI | `io.insert-koin:koin-android` | **4.1.1** |
| Coroutines | `org.jetbrains.kotlinx:kotlinx-coroutines-*` | **1.10.2** |
| Serialization | `kotlinx-serialization-json` | **1.10.0** |
| Images | `io.coil-kt:coil-compose` | **2.6.0** |
| Location | `com.google.android.gms:play-services-location` | **21.3.0** |
| QR scan (store variant) | `com.google.mlkit:barcode-scanning` | **17.3.0** |
| QR render | `com.google.zxing:core` | **3.5.4** |
| Zcash | `cash.z.ecc.android:zcash-android-sdk` | **2.4.8** |
| Zcash | `cash.z.ecc.android:kotlin-bip39` | **1.0.9** |
| Crypto | `com.google.crypto.tink:tink-android` | **1.20.0** |
| Keystone | `com.github.KeystoneHQ:keystone-sdk-android` | **0.8.3** |
| Flexa | `co.flexa:core / spend` | **1.1.2** |
| Animations | `com.airbnb.android:lottie-compose` | **6.6.4** |
| Desugaring | `com.android.tools:desugar_jdk_libs` | **2.1.5** |

To confirm any of these on your machine: `./gradlew :ui-lib:dependencies --configuration zcashmainnetStoreDebugRuntimeClasspath`.

## Building for Testnet

The app supports both Zcash Mainnet and Testnet. Network is selected at build time via
the `network` product flavor (`zcashmainnet` / `zcashtestnet`) and the two apps can be
installed side-by-side — testnet builds get a `.testnet` application-ID suffix.

Pick a variant directly:

```sh
./gradlew :app:assembleZcashtestnetStoreDebug
```

Or use the `ZCASH_NETWORK` switch in `gradle.properties` (or `-P` / env var) so the
short task names resolve to a single network:

```sh
# build a testnet debug app — no need to spell out the full variant name
./gradlew -PZCASH_NETWORK=testnet :app:assembleDebug
```

Valid values are `mainnet`, `testnet`, or blank (keep both). The selection is the single
source of truth for both `BuildConfig.FLAVOR_network` and the runtime `R.bool.zcash_is_testnet`
resource — they can't drift. Testnet builds connect to `testnet.zec.rocks:443` by default
(see `ui-lib/.../LightWalletEndpointProvider.kt`).

## One-shot sanity-check build

A clean build from a fresh checkout should succeed with:

```sh
./gradlew :app:assembleZcashmainnetStoreDebug
```

If this fails, verify in order:
1. `./gradlew --version` prints `Gradle 8.14.4` and `Launcher JVM: 17.x`.
   (`gradle/daemon-jvm.properties` should select JDK 17 automatically; if it
   shows a higher JDK, set `JAVA_HOME` to a JDK 17 install and stop the daemon
   with `./gradlew --stop` before retrying.)
2. `$ANDROID_HOME/ndk/27.0.12077973` exists.
3. `../bare-kit` (holepunchto/bare-kit v2.0.0) and `../zappMessaging` exist as
   siblings. Check `.zapp-deps` for the expected commit SHAs.
4. `gradle.properties` has not been locally modified.

# Forking

If you plan to fork the project to create a new app of your own, please make
the following changes.  (If you're making a GitHub fork to contribute back to
the project, these steps are not necessary.)

1. Change the app name under [gradle.properties](gradle.properties)
    1. See `ZCASH_RELEASE_APP_NAME`
1. Change the package name under [app/build.gradle.kts](app/build.gradle.kts)
    1. See `ZCASH_RELEASE_PACKAGE_NAME`
1. Change the support email address under [strings.xml](ui-lib/src/main/res/ui/non_translatable/values/strings.xml)
    1. See `support_email_address`
1. Remove any copyrighted ZCash icons, logos, or assets
    1. ui-lib/src/main/res/common/ - All of the the ic_launcher assets
1. Optional
    1. Configure secrets and variables for [Continuous Integration](docs/CI.md)
    1. Configure Firebase API keys and place them under `app/src/debug/google-services.json` and `app/src/release/google-services.json`

# Known Issues

1. During builds, a warning will be printed that says "Unable to detect AGP
   versions for included builds. All projects in the build should use the same
   AGP version."  This can be safely ignored.  The version under
   build-conventions is the same as the version used elsewhere in the
   application.
1. When the code coverage Gradle property
   `IS_ANDROID_INSTRUMENTATION_TEST_COVERAGE_ENABLED` is enabled, the debug app
   APK cannot be run.  The coverage flag should therefore only be set when
   running automated tests.
1. Test coverage for Compose code will be low, due to [known limitations](https://github.com/jacoco/jacoco/issues/1208) in the interaction between Compose and Jacoco.
1. Adding the `espresso-contrib` dependency will cause builds to fail, due to conflicting classes.  This is a [known issue](https://github.com/zcash/zcash-android-wallet-sdk/issues/306) with the Zcash Android SDK.
1. During app first launch, the following exception starting with `AndroidKeysetManager: keyset not found, will generate a new one` is printed twice.  This exception is not an error, and the code is not being invoked twice.
