# Pheromone

Minimal Pheromone KMP mobile application skeleton.

A runnable Kotlin Multiplatform starter for mobile (Android + iOS) with an offline-first
infrastructure, auth framework, and sync pipeline. Add business modules to it when
the `metaphor add module ... --to pheromone` generator lands (or by hand).

## Docs

- [docs/GETTING_STARTED.md](docs/GETTING_STARTED.md) — prerequisites, first build, per-platform run instructions
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — layers, package layout, MVI, offline-first
- [docs/INFRASTRUCTURE.md](docs/INFRASTRUCTURE.md) — DI, HTTP, DB, storage, sync, monitoring, push, feature flags
- [docs/ADDING_A_MODULE.md](docs/ADDING_A_MODULE.md) — how to add a business bounded context
- [docs/TESTING.md](docs/TESTING.md) — test source sets, recipes, CI

## Run

```bash
# Android
./gradlew :android:assembleDebug
./gradlew :android:installDebug    # with a device / emulator attached

# iOS — supply your own Xcode project that links against shared.framework
./gradlew :shared:iosSimulatorArm64MainBinaries
```

## What ships in the skeleton

- KMP shared module (`shared/`) — `commonMain`, `androidMain`, `iosMain`
- Android app entry (`android/`) — Compose, Koin DI, edge-to-edge Material 3
- iOS stubs (`ios/iosApp/`) — Swift app delegate, Keychain helper, `Info.plist`
- **Core**: BaseViewModel (MVI), CRUD API base, mappers, validators, error types, logger
- **Infrastructure**:
  - Ktor HTTP client factory (authenticated + unauthenticated) with token refresh
  - SQLDelight DB (core tables only: `Todo`, cache, sync outbox/history/conflict, media cache)
  - DataStore / SharedPreferences / Keychain storage abstraction
  - Auth repo skeleton with biometric support
  - Sync engine (`OutboxManager`, `SyncEngine`, `SyncWorker`, `VersionTracker`)
  - Monitoring (`CrashReporter`, `Analytics`, `PerformanceMonitor`) as no-op stubs
  - Connectivity monitor, feature flags, crypto/cache encryptor
- **Presentation**: theme + a reusable Compose component library (inputs, cards,
  charts, feedback, media, tree, etc.). Entry screen is a bare "Pheromone" label.
- **Config**: Detekt rules, gradle wrapper 8.13, Kotlin 2.x, Compose Multiplatform

## What is NOT in the skeleton

- No business modules (no bersihir, bucket, corpus, sapiens)
- No business screens (orders, inventory, settings, notifications, dashboards, …)
- No navigation graph — only `MainActivity` renders a single placeholder Compose screen
- No Xcode project — bring your own `iosApp.xcodeproj` that links `shared.framework`
- No `google-services.json` — the google-services plugin and firebase-messaging
  dep are commented out in `android/build.gradle.kts`; re-enable once you add your
  own file
- No OAuth, no role/permission gating, no session context

## Known carry-over issues

- **iOS cinterop compile**: `:shared:compileKotlinIosSimulatorArm64` currently
  fails with `@OptIn(ExperimentalForeignApi::class)` missing in a handful of
  `iosMain` files (`BiometricAuthManager.ios.kt`, `CryptoProvider.ios.kt`,
  `ConnectivityMonitor.ios.kt`). These were present in the upstream mobileapp
  and were not fixed during skeleton extraction. Add the opt-in or patch the
  cinterop usage when you wire up iOS.
- **Android `MainActivity`**: wired to a single placeholder Compose screen.
  Replace with your own navigation component when adding modules.

## Package & naming

- Root package: `id.startapp.pheromone.*`
- Gradle root project: `pheromone`
- Android `applicationId`: `id.startapp.pheromone` (debug suffix `.debug`)
- Deep-link scheme: `mobileapp` (kept generic from upstream — rename to `pheromone`
  in `AndroidManifest.xml`, `Info.plist`, and `AppDelegate.swift` if you prefer)

Modules you add should use `id.startapp.pheromone.<module>.*` and plug into DI
through new Koin modules passed to `initKoin { modules(...) }`.

## Development

Override API URLs in `local.properties` (gitignored):

```properties
API_BASE_URL=http://192.168.1.50:3000
GRPC_WEB_URL=http://192.168.1.50:50051
GOOGLE_MAPS_API_KEY=your_key_here
```

Android emulator defaults to `http://10.0.2.2:3000` (the emulator host loopback).
