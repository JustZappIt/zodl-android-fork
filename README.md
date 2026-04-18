# Zodl Android Wallet

This is the official home of the Zodl Zcash wallet for Android, a no-frills
Zcash mobile wallet leveraging the [Zcash Android SDK](https://github.com/zcash/zcash-android-wallet-sdk).

# Download

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/co.electriccoin.zcash.foss/)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
     alt="Get it on Google Play"
     height="80">](https://play.google.com/store/apps/details?id=co.electriccoin.zcash)

Or download the latest APK from the [Releases Section](https://github.com/zodl-inc/zashi-android/releases/latest).

# Zodl Discord

Join the Zodl community on Discord to report bugs, share ideas, request new
features, and help shape Zodl's journey!

https://discord.gg/jQPU7aXe7A

# Reporting an issue

If you'd like to report a technical issue or feature request for the Android
Wallet, please file a GitHub issue [here](https://github.com/zodl-inc/zodl-android/issues/new/choose).

For feature requests and issues related to the Zodl user interface that are
not Android-specific, please file a GitHub issue [here](https://github.com/zodl-inc/zodl-project/issues/new/choose).

If you wish to report a security issue, please follow our
[Responsible Disclosure guidelines](https://github.com/zodl-inc/zodl-project/blob/master/responsible_disclosure.md).
See the [Wallet App Threat Model](https://github.com/zodl-inc/zodl-project/blob/master/wallet_threat_model.md)
for more information about the security and privacy limitations of the wallet.

General Zcash questions and/or support requests and are best directed to either:
 * [Zcash Forum](https://forum.zcashcommunity.com/)
 * [Discord Community](https://discord.io/zcash-community)

# Contributing

Contributions are very much welcomed!  Please read our 
[Contributing Guidelines](docs/CONTRIBUTING.md) to learn about our process.

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
| JDK | **17.0.16 (Azul Zulu, aarch64)** — any OpenJDK 17 LTS build works (Temurin is a good alternative) | Gradle uses a **toolchain of JDK 17** (`JVM_TOOLCHAIN=17` in `gradle.properties`), so Gradle will auto-provision a matching JDK if one isn't on `PATH`. `JAVA_HOME` on the host should still point at a JDK 17 install for command-line `./gradlew` to work on first launch. |
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
git clone https://github.com/JustZappIt/bare-kit.git       ../bare-kit
git clone https://github.com/JustZappIt/zappMessaging.git  ../zappMessaging
```

(Repo URLs may differ depending on your fork; confirm with the team before
cloning public mirrors.)

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
1. `java -version` prints a `17.x` JDK.
2. `./gradlew --version` prints `Gradle 8.14.4` and `Launcher JVM: 17.x`.
3. `$ANDROID_HOME/ndk/27.0.12077973` exists.
4. `../bare-kit` and `../zappMessaging` exist as siblings to `zodl-android`.
5. `gradle.properties` has not been locally modified.

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
