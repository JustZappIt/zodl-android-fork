# Zodl Android - Developer Setup

This is a fork of Zodl (Zcash wallet) with Zapp's P2P messaging integrated. The build requires specific tool versions due to the native bare-kit module (Hyperswarm P2P runtime).

## Required Versions

| Tool | Version | Notes |
|---|---|---|
| **JDK** | **17** (exactly) | JDK 21 will fail. Use Azul Zulu, Temurin, or Oracle JDK 17. |
| **Android Studio** | Meerkat (2024.3.1) or newer | Needs AGP 8.13.x support |
| **Gradle** | 8.14.4 | Bundled via wrapper (`./gradlew`) - don't install separately |
| **Android SDK** | API 36 (compile), API 27+ (min) | Install via SDK Manager |
| **Android NDK** | **28.2.13676358** | Required by bare-kit native build |
| **CMake** | **4.1.2+** | Required by bare-kit native build |
| **Kotlin** | 2.3.10 | Managed by Gradle, no manual install needed |

## Step-by-Step Setup

### 1. Install JDK 17

The project **must** use JDK 17. JDK 21+ causes `jvmTarget` compilation errors.

```bash
# macOS (Homebrew)
brew install --cask zulu17

# Verify
java -version
# Should show: openjdk version "17.x.x"
```

If you have multiple JDKs, set `JAVA_HOME`:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

Or in Android Studio: **Settings > Build > Gradle > Gradle JDK** -> select JDK 17.

### 2. Install Android SDK Components

Open Android Studio **SDK Manager** (Settings > Languages & Frameworks > Android SDK) and install:

**SDK Platforms tab:**
- Android 14.0 (API 36) - or just check "Show Package Details" and install `Android SDK Platform 36`

**SDK Tools tab (check "Show Package Details"):**
- Android SDK Build-Tools 36.x
- **NDK (Side by side) > 28.2.13676358** (critical - bare-kit needs this exact version)
- **CMake > 4.1.2** (critical - bare-kit needs CMake 4.0+)
- Android SDK Command-line Tools
- Android Emulator

Or install via command line:
```bash
sdkmanager "ndk;28.2.13676358" "cmake;4.1.2" "platforms;android-36" "build-tools;36.0.0"
```

### 3. Clone the Repo

The project expects sibling directories for the messaging modules **and** the
Zcash Android SDK (consumed via Gradle `includeBuild`, see step 3a):

```
dev/zapp/
  zodl-android/                 # This repo
  zappMessaging/                # P2P messaging SDK (android/ subdirectory)
  bare-kit/                     # BareKit JS runtime (android/ subdirectory)
  zcash-android-wallet-sdk/     # Zcash SDK source — built locally, not from Maven
```

```bash
cd ~/dev/zapp
git clone <zodl-android-repo-url> zodl-android
git clone <zappMessaging-repo-url> zappMessaging
git clone <bare-kit-repo-url> bare-kit
git clone https://github.com/zcash/zcash-android-wallet-sdk.git
```

### 3a. Pin the Zcash SDK SHA

Upstream `zodl-inc/zodl-android` `main` builds against unreleased commits of
the Zcash Android SDK that are not yet published to Maven Central
(`ZcashDecimalFormatSymbols`, `zatoshiFormatter`, etc. live on the SDK's `main`
branch but not in tag `v2.4.8`). Building our fork from Maven would fail the
same way upstream's CI fails on `pull-request.yml` — verified directly. The
fix is the official `SDK_INCLUDED_BUILD_PATH` mechanism documented in
upstream's `docs/Setup.md`.

After cloning the SDK, check out the SHA pinned in `.zapp-deps`:

```bash
cd ~/dev/zapp/zcash-android-wallet-sdk
git checkout $(grep ^zcashAndroidWalletSdk= ../zodl-android/.zapp-deps | cut -d= -f2)
```

The `gradle.properties` in this repo already sets:

```
SDK_INCLUDED_BUILD_PATH=../zcash-android-wallet-sdk
```

That tells Gradle to compile the SDK from source on every build instead of
downloading the Maven jar. **APK size is unchanged** — only the *origin* of the
SDK's compiled `.class` files changes. First build is slower (Rust toolchain
compiles the SDK's native backend); subsequent builds are cached.

Requirements added by this step:
- **Rust toolchain** (`cargo`, `rustc`) — `brew install rust` if missing.
- **Disk** ~1.7 GB for the SDK clone (native blockchain checkpoints).

To revert to Maven, blank the line: `SDK_INCLUDED_BUILD_PATH=`. Builds will
then fail until upstream publishes a new SDK release with the missing symbols.

### 4. Build

```bash
cd zodl-android

# First build (downloads dependencies, compiles native code - ~15 min)
./gradlew :app:assembleZcashmainnetStoreDebug

# Install on connected device/emulator
./gradlew :app:installZcashmainnetStoreDebug
```

### 5. Build Variant

In Android Studio, use this build variant:
- **Module:** app
- **Build Variant:** `zcashmainnetStoreDebug`

Set via: **Build > Select Build Variant** (or the Build Variants panel on the left sidebar).

## Common Build Errors

### `jvmTarget` / JVM compilation error
```
Using 'jvmTarget: String' is an error. Please migrate to the compilerOptions DSL.
```
**Fix:** You're using the wrong JDK version. Must be JDK 17, not 21+. Check `java -version` and Android Studio's Gradle JDK setting.

### `NDK not found` or CMake errors
```
No version of NDK matched the requested version 28.2.13676358
```
**Fix:** Install the exact NDK version via SDK Manager:
```bash
sdkmanager "ndk;28.2.13676358"
```

### `CMake 4.0.0+ required`
```
CMake 4.0.0 or higher is required
```
**Fix:** Install CMake 4.1.2 via SDK Manager:
```bash
sdkmanager "cmake;4.1.2"
```

### Manifest merger: minSdkVersion 27 < 29
```
uses-sdk:minSdkVersion 27 cannot be smaller than version 29 declared in library [:zappmessaging]
```
**Fix:** Already handled in `app/src/main/AndroidManifest.xml` with `tools:overrideLibrary`. If you see this, make sure you have the latest code.

### `Could not resolve` / dependency resolution failures
```
Could not resolve all files for configuration ':app:coreLibraryDesugaring'
```
**Fix:** Corrupted Gradle cache. Run:
```bash
./gradlew --stop
rm -rf ~/.gradle/caches/transforms-*/ ~/.gradle/caches/8.14.4/
./gradlew :app:assembleZcashmainnetStoreDebug
```

### `Plugin 'secant.detekt-conventions' not found`
**Fix:** Stale build plugin cache:
```bash
rm -rf zodl-android/.gradle/ zodl-android/build-conventions-secant/build/ zodl-android/buildSrc/build/
./gradlew :app:assembleZcashmainnetStoreDebug
```

## Project Structure

```
zodl-android/
  app/                          # Main app module
  ui-lib/                       # All UI screens (including chat)
    src/main/java/.../screen/chat/  # P2P chat screens (ported from Zapp)
      AndroidChat.kt            # Navigation routes & entry points
      model/ChatModels.kt       # Chat data models
      viewmodel/ChatViewModel.kt # Chat business logic
      view/                     # UI composables
        ChatListView.kt         # Conversation list
        ChatRoomView.kt         # Message room
        ChatContactsView.kt     # Contacts list
        ChatProfileView.kt      # User profile
        ChatSettingsView.kt     # Chat settings + network status
        ChatIdentitySetupView.kt # Identity creation (fallback)
        ContactEditView.kt      # Edit/delete contact
        NewConversationView.kt  # Start new chat
  ../zappMessaging/android/     # P2P messaging SDK (external module)
  ../bare-kit/android/          # BareKit JS runtime (external module)
```

## Architecture Notes

- **DI:** Koin (not Hilt) - chat modules registered in `ZappMessagingModule.kt`
- **Navigation:** Jetpack Compose Navigation with `@Serializable` route args
- **P2P Engine:** Hyperswarm DHT via BareKit JS worklet (runs JavaScript in a native VM)
- **Identity:** Ed25519 keypair derived from BIP39 seed phrase
- **Chat identity** is auto-created when wallet is created/restored (see `WalletViewModel.kt`)
