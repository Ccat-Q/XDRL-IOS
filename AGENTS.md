# AGENTS.md

XDYL (Nebula Updater 星云更新器) — Minecraft mod downloader. Kotlin Multiplatform + Compose Multiplatform (Kotlin 1.9.22, Compose 1.6.0, Gradle 8.9, JDK 17) for iOS, plus a parallel Android/desktop app.

## Two independent Gradle builds — do not confuse them

- **Root build** (`./gradlew` from repo root): modules `:shared` (KMP iOS framework, **static**, contains ALL UI in Compose) and `:iosApp` (thin Xcode wrapper). Pure KMP — no Android module, no `local.properties` needed.
- **`xdyl-main/` is a separate standalone Gradle build** with its own `gradlew`, its own `settings.gradle.kts` (also named `xdyl`), for Android `:app` + desktop `:desktop`. It is NOT included in the root build — root commands cannot see it. Run `xdyl-main/gradlew` from `xdyl-main/`.
  - Android: AGP 8.2.2, minSdk 28 / target+compileSdk 35, namespace `www.xdyl.hygge.com`. Needs Android SDK (`local.properties` or `ANDROID_HOME`).
  - Desktop: Compose JVM app, main class `www.xdyl.hygge.desktop.MainKt`, packaging via `:desktop:packageMsi` (Windows MSI only).

## iOS build (root) — framework must be built before Xcode

- Swift (`iosApp/iosApp/iOSApp.swift`) only wraps `PlatformKt.MainViewController()` from the `shared` framework; all real code is Compose in `shared/src/commonMain`.
- The Xcode project has **no Gradle build phase** (no `embedAndSignAppleFrameworkForXcode` script). It links the pre-built static framework from `shared/build/bin/iosArm64/debugFramework` via `FRAMEWORK_SEARCH_PATHS` + `OTHER_LDFLAGS -framework shared`. **Always run `./gradlew :shared:linkDebugFrameworkIosArm64` before `xcodebuild`** or linking fails — CI does exactly this.
- The framework search path is `iosArm64/debugFramework` in both Debug and Release — **device-only**. Simulator builds will not link without editing the pbxproj.
- Full CI-equivalent: `xcodebuild archive -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphoneos -configuration Debug -archivePath build/iosApp.xcarchive CODE_SIGNING_ALLOWED=NO`
- Deployment target iOS 16.0, bundle id `www.xdyl.hygge.iosApp`. `Info.plist` intentionally sets `NSAllowsArbitraryLoads` (plain-HTTP download servers) and `UIFileSharingEnabled` (file App integration).

## CI

- Only `.github/workflows/ios.yml` is active (macos-latest: build `:shared:linkDebugFrameworkIosArm64` → xcodebuild archive → IPA artifact).
- `xdyl-main/.github/workflows/android.yml` is **dead config**: GitHub Actions only discovers `.github/workflows/` at the repo root, so the Android/Windows jobs never run. Don't rely on it or add "CI will catch this" assumptions for the nested build.

## Signing material is committed to git — handle carefully

- Tracked signing files (do NOT move, delete, or "clean up"): root `cert.pem`, `certificate.pfx`, `key.pem`; `xdyl-main/cert.pem`, `certificate.pfx`, `key.pem`; `xdyl-main/app/xdyl_keystore.jks`.
- Android release signing is hardcoded in `xdyl-main/app/build.gradle.kts` (keystore `xdyl_keystore.jks`, password/alias values in plaintext); both debug and release build types use it. Never log or echo these values, and never change them without coordinating release signing.

## Verification & conventions

- **No tests exist** anywhere in either build, and no lint/format config beyond `kotlin.code.style=official`. Verification = Gradle compile/link + `xcodebuild` for iOS, `assembleDebug` for Android.
- Package roots: `www.xdyl.hygge.shared` (iOS/shared), `www.xdyl.hygge.com` (Android), `www.xdyl.hygge.desktop`.
- Keep files UTF-8 — `-Dfile.encoding=UTF-8` is set in both `gradle.properties` (a prior README encoding fix exists).
- Do not bump Gradle/Kotlin/Compose versions: both builds pin the same toolchain (Gradle 8.9, Kotlin 1.9.22, Compose 1.6.0).
