# Architecture

Pheromone follows DDD + Clean Architecture on Kotlin Multiplatform, structured as a modular monolith. It mirrors the backbone (Rust) architecture on the mobile side: every business module owns its own domain, bounded contexts do not leak entities, and the infrastructure layer is shared framework code.

## Layers

```
┌─────────────────────────────────────────┐
│ presentation   MainActivity, Compose UI │   Android + iOS entry points
├─────────────────────────────────────────┤
│ application    use cases, validators    │   Orchestration + security
├─────────────────────────────────────────┤
│ domain         entities, repositories   │   Pure business contracts
├─────────────────────────────────────────┤
│ infrastructure http, db, sync, storage  │   I/O + platform integration
├─────────────────────────────────────────┤
│ core           ViewModel, CRUD, logger  │   Framework primitives
└─────────────────────────────────────────┘
```

The arrows only point down: `presentation` depends on `application`, `application` on `domain`, `infrastructure` implements `domain` interfaces, `core` sits underneath. A use case never imports from `presentation`; a repository interface never imports from `infrastructure`.

## Package layout

```
id.startapp.pheromone
├── core/                  framework primitives
│   ├── api/               BaseCrudApiClient — paginated CRUD over HTTP
│   ├── error/             sealed errors
│   ├── logging/           KMP logger
│   ├── mapper/            DTO ↔ entity conversion helpers
│   ├── service/           cross-cutting service base classes
│   ├── usecase/           UseCase<Input, Output> interface + helpers
│   ├── validator/         Validator<T, E> + result types
│   └── viewmodel/         BaseViewModel (MVI: state/intent/effect)
│
├── application/           orchestration
│   ├── security/          RateLimiter, token helpers
│   ├── usecases/auth/     Login / Logout / Refresh / Register / Verify ...
│   ├── utils/             formatDuration etc.
│   └── validators/        FieldValidators, PasswordValidator
│
├── domain/                business contracts (no Ktor, no Android)
│   ├── auth/entity/       AuthToken, User, RegisterResult, VerifyEmailResult
│   ├── auth/repository/   AuthRepository interface
│   ├── demo/              Todo sample — remove when you add a real module
│   └── types/             Result, NetworkError
│
├── infrastructure/        implementations
│   ├── auth/              BiometricAuthManager (expect/actual)
│   ├── cache/             CacheTTL policy
│   ├── crypto/            CryptoProvider, CacheEncryptor
│   ├── database/          DatabaseManager, dao/*
│   ├── di/                DiModule.kt — Koin modules
│   ├── events/            AppEventBus
│   ├── featureflags/      FeatureFlagManager + NoOp remote config
│   ├── media/             MediaCacheManager
│   ├── monitoring/        CrashReporter, Analytics, PerformanceMonitor (stubs)
│   ├── network/           HttpClientFactory, ConnectivityMonitor, api/*
│   ├── pagination/        cursor + offset helpers
│   ├── push/              PushNotificationManager (expect/actual)
│   ├── repository/        AuthRepositoryImpl, TodoRepositoryImpl, UserProfileCache
│   ├── storage/           KeyValueStorage, SecureStorage, TokenStorage
│   └── sync/              OutboxManager, SyncEngine, SyncWorker, VersionTracker
│
└── presentation/          UI
    ├── ui/theme/          AppTheme, AppColors, Typography, Shapes
    ├── ui/components/     Compose library (inputs, cards, charts, map, media…)
    ├── ui/extensions/     Modifier helpers
    ├── ui/accessibility/  TestTags, a11y wrappers
    ├── util/              ReceiptFormatter, printing/*
    └── viewmodel/         BaseViewModel lives here
```

Android-only code lives under `shared/src/androidMain/kotlin/id/startapp/pheromone/…`, iOS-only under `shared/src/iosMain/kotlin/…`. The KMP `expect/actual` contracts are declared in `commonMain` and implemented per platform.

## Source sets

| Source set          | Purpose                                              |
|---------------------|------------------------------------------------------|
| `commonMain`        | Platform-agnostic Kotlin                             |
| `androidMain`       | Android implementations (Ktor OkHttp, SQLDelight Android driver, EncryptedSharedPreferences, WorkManager) |
| `iosMain`           | iOS implementations (Ktor Darwin, SQLDelight Native driver, Keychain, NSUserDefaults) |
| `commonTest`        | Shared unit tests                                    |
| `androidTest`       | Instrumented tests                                   |

## MVI with BaseViewModel

`core/viewmodel/BaseViewModel.kt` implements Model-View-Intent. State is a sealed class, intents are user/system events, effects are one-shots (navigation, toast).

```kotlin
class SplashViewModel : BaseViewModel<SplashState, SplashIntent, SplashEffect>() {
    override fun initialState() = SplashState.Idle

    override fun handleIntent(intent: SplashIntent) = when (intent) {
        is SplashIntent.Initialize -> launch { /* ... */ setState { SplashState.Ready } }
    }
}
```

## Offline-first + sync

Core DAOs (`CacheDao`, `OutboxDao`, `ConflictDao`, `SyncHistoryDao`, `MediaCacheDao`) are in place, together with the sync pipeline:

- `OutboxManager` — queues mutations when offline
- `SyncEngine` — drains the outbox when connectivity returns
- `SyncEntityRegistry` — the extension point where modules register their sync handlers
- `SyncWorker` (Android WorkManager-backed) — periodic reconciliation
- `ConflictDao` + `VersionTracker` — optimistic concurrency

The skeleton does not ship any concrete `SyncHandler`. Modules add their own by calling `SyncEntityRegistry.register(...)` in their DI module.

## DI (Koin)

`infrastructure/di/DiModule.kt` composes five top-level modules:

- `platformModule` (expect/actual): SqlDriver, SecureStorage, TokenStorage, KeyValueStorage, ConnectivityMonitor
- `storageModule`: feature flags
- `databaseModule`: DAOs + sync components
- `networkModule`: authenticated/unauthenticated HttpClients, AuthApiClient, PushTokenApiClient
- `repositoryModule`: UserProfileCache, AuthRepositoryImpl, TodoRepositoryImpl
- `useCaseModule`: the nine auth use cases

New modules add their own Koin modules in `initKoin { modules(…) }` — see [ADDING_A_MODULE.md](ADDING_A_MODULE.md).

## Dependency policy

- `domain` has no dependencies on any other layer or third-party framework (pure Kotlin + kotlinx.serialization/datetime)
- `application` depends only on `domain` + kotlinx.coroutines
- `infrastructure` depends on `domain` + Ktor + SQLDelight + platform SDKs
- `presentation` depends on `application` + Compose + Koin
- `core` is imported by any layer (it's the framework substrate)

No layer imports upward. Use cases do not call other use cases directly — compose by chaining results.
