# Getting Started

## Prerequisites

| Tool                    | Version    | Notes                                                    |
|-------------------------|------------|----------------------------------------------------------|
| JDK                     | 17         | Temurin/Adoptium recommended. Kotlin 2.x targets 17.     |
| Android Studio          | Hedgehog+  | Bundles the SDK; Ladybug+ if you want Compose previews.  |
| Android SDK             | API 35     | `compileSdk = 35`, `minSdk = 24`.                        |
| Xcode                   | 15+        | Only if you target iOS.                                  |
| Kotlin                  | handled by Gradle | Don't install globally — wrapper pins the version. |

macOS with Apple Silicon is the reference environment; Linux and Intel Macs work for Android-only builds.

## First-time setup

```bash
git clone <your-fork>/pheromone
cd pheromone

# 1. Point Gradle at your JDK 17 (only if auto-detection fails):
#    edit gradle.properties and uncomment org.gradle.java.home

# 2. Create local.properties for device/emulator API endpoints.
#    This file is gitignored.
cat > local.properties <<'EOF'
sdk.dir=/Users/you/Library/Android/sdk
API_BASE_URL=http://10.0.2.2:3000
GRPC_WEB_URL=http://10.0.2.2:50051
# GOOGLE_MAPS_API_KEY=...  (optional)
EOF

# 3. Sync Gradle (downloads deps — first run takes a few minutes).
./gradlew --no-daemon help
```

## Run on Android

```bash
# Build the debug APK
./gradlew :android:assembleDebug

# Install on a connected device/emulator
./gradlew :android:installDebug

# Or let Android Studio's Run button do both (faster incremental builds)
```

The APK output is `android/build/outputs/apk/debug/android-debug.apk`. The debug variant has `applicationId = id.startapp.pheromone.debug` so it coexists with a release build.

### Emulator host loopback

- Android emulator → the host machine is `10.0.2.2`, so `API_BASE_URL=http://10.0.2.2:3000` reaches a backend running on `localhost:3000`.
- Physical Android device → use your machine's LAN IP (`192.168.x.y`) and add it to `android/src/main/res/xml/network_security_config.xml` under `<domain-config>`.

## Run on iOS

The skeleton does not ship an `ios/iosApp.xcodeproj`. You bring your own project that links against the KMP-produced `shared.framework`:

```bash
# Build the framework for the iOS Simulator (Apple Silicon)
./gradlew :shared:iosSimulatorArm64MainBinaries

# The framework lands at:
#   shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework
```

Create an Xcode app project and add `shared.framework` to Framework Search Paths + Linked Frameworks. The Swift stubs under `ios/iosApp/` (`AppDelegate.swift`, `KeychainHelper.swift`, `Info.plist`) can be copied into the new project as a starting point.

> Known carry-over: `:shared:compileKotlinIosSimulatorArm64` currently fails — see the *Known carry-over issues* section in [../README.md](../README.md). Add `@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)` to the three offending iOS files when you wire up iOS support.

## Override configuration

Values are resolved in this order (later wins):

1. `gradle.properties` (committed defaults)
2. `local.properties` (developer overrides, gitignored)
3. Environment variables picked up by `project.findProperty(...)`

Exposed to Kotlin via `BuildConfig`:

- `API_BASE_URL` — REST base
- `GRPC_WEB_URL` — gRPC-Web endpoint
- `GOOGLE_MAPS_API_KEY` — surfaced as a manifest placeholder, consumed by `AndroidManifest.xml` `<meta-data>`
- `IS_DEBUG` — true in debug builds, false in release

## Quality gates

```bash
./gradlew detekt                 # static analysis (config/detekt/detekt.yml)
./gradlew :shared:allTests       # commonTest + androidUnitTest + iosTest
./gradlew :android:lint          # Android lint
```

`detekt` and `lint` are best-effort in the skeleton — the shared rule set is inherited from the upstream mobileapp.

## Troubleshooting

**`org.gradle.java.home must point to a JDK`** — comment out the `org.gradle.java.home` line in `gradle.properties`, or update its path. Gradle 8.x auto-detects JDKs on standard paths.

**`Unresolved reference 'Volatile'`** — you're on an older Kotlin. The skeleton targets Kotlin 2.x where `@Volatile` moved to `kotlin.concurrent.Volatile`.

**`google-services plugin failed`** — the plugin is disabled by default (commented out in `android/build.gradle.kts`). Leave it commented until you drop in a real `google-services.json`.

**Out-of-memory during build** — bump `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx8g -XX:MaxMetaspaceSize=1536m
kotlin.daemon.jvmargs=-Xmx8g -XX:MaxMetaspaceSize=1536m
```

**`sqlite database is locked`** on the emulator — kill the app and wipe app data; SQLDelight doesn't like a half-finished migration.
