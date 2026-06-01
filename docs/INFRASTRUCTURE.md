# Infrastructure

What lives under `shared/src/commonMain/kotlin/id/startapp/infrastructure/` and how to use it. Everything here is framework-level — modules consume it; they do not replace it.

## Koin modules

All DI lives in [infrastructure/di/DiModule.kt](../shared/src/commonMain/kotlin/id/startapp/infrastructure/di/DiModule.kt). The entry point is `initKoin`:

```kotlin
fun initKoin(config: KoinAppDeclaration = {}) {
    startKoin {
        config()
        modules(platformModule, storageModule, databaseModule,
                networkModule, repositoryModule, useCaseModule)
    }
}
```

| Module              | Registers                                                         |
|---------------------|-------------------------------------------------------------------|
| `platformModule`    | `SqlDriver`, `SecureStorage`, `TokenStorage`, `KeyValueStorage`, `ConnectivityMonitor` (expect/actual) |
| `storageModule`     | `FeatureFlagManager`, `NoOpRemoteConfigProvider`                  |
| `databaseModule`    | DAOs, `OutboxManager`, `SyncEngine`, `SyncEntityRegistry`, `SyncWorker`, `VersionTracker`, `CryptoProvider`, `CacheEncryptor`, `BiometricAuthManager`, `MediaCacheManager`, `SyncStateHolder` |
| `networkModule`     | `HttpClientFactory`, `InMemoryCache`, `AppEventBus`, two `HttpClient` instances (`named("authenticated")`, `named("unauthenticated")`), `AuthApiClient` + `AuthApi` binding, `TokenRefreshProvider`, `PushTokenApiClient` |
| `repositoryModule`  | `UserProfileCache`, `AuthRepositoryImpl` bound to `AuthRepository`, `TodoRepositoryImpl` bound to `TodoRepository` |
| `useCaseModule`     | The nine auth use cases (`LoginUseCase` etc.)                     |

Your business module adds its own module and passes it via `initKoin { modules(catalogModule) }`.

## HTTP

[`HttpClientFactory`](../shared/src/commonMain/kotlin/id/startapp/infrastructure/network/HttpClientFactory.kt) creates Ktor clients. Two variants:

- **Unauthenticated** (`get(named("unauthenticated"))`) — used only by the refresh-token flow to avoid recursion.
- **Authenticated** (`get(named("authenticated"))`) — installs a token interceptor that attaches `Authorization: Bearer …`, retries once on 401 by invoking `TokenRefreshProvider`, and broadcasts session-invalid events via `AppEventBus`.

`BuildConfig.API_BASE_URL` is the base URL. Override per-environment via `local.properties` (see [GETTING_STARTED.md](GETTING_STARTED.md)).

For tracing, add `HttpClientFactory.setDeviceIdentity(deviceId, deviceName)` at app startup — the skeleton does this in `MobileApplication.onCreate`.

## Database

SQLDelight is configured in `shared/build.gradle.kts`. Core schema:

- `AppDatabase.sq` — root DB file
- `TodoQueries.sq` — demo entity (remove when you ship a real module)
- `CacheQueries.sq` — generic offline cache (keyed by entity + id)
- `MediaCacheQueries.sq` — binary blobs (images, files)
- `SyncOutboxQueries.sq` — pending mutations
- `SyncConflictQueries.sq` — version mismatches
- `SyncHistoryQueries.sq` — audit trail

DAOs in [infrastructure/database/dao/](../shared/src/commonMain/kotlin/id/startapp/infrastructure/database/dao/) wrap the generated `AppDatabase`. Add your own DAOs alongside.

`DatabaseManager` holds the driver as a singleton. The driver is injected by `platformModule` (`AndroidSqliteDriver` on Android, `NativeSqliteDriver` on iOS).

### Migrations

Add a `.sqm` file under `shared/src/commonMain/sqldelight/migrations/` for every schema change, and bump the version in the `sqldelight { }` block. SQLDelight runs migrations automatically on first open.

## Storage

Three abstractions — all three are bound in `platformModule`:

| Interface         | Purpose                       | Android impl                            | iOS impl           |
|-------------------|-------------------------------|-----------------------------------------|--------------------|
| `SecureStorage`   | Tokens, sensitive values      | `EncryptedSharedPreferences` (AES-256-GCM) | Keychain        |
| `TokenStorage`    | Auth tokens only (typed)      | Delegates to `SecureStorage`            | Delegates to Keychain |
| `KeyValueStorage` | User prefs, app state         | `SharedPreferences`                     | `NSUserDefaults`   |

Never write a token to `KeyValueStorage`. Never write a large blob to `SecureStorage` (it's a KV store with tight size limits).

## Sync

Offline-first pattern, driven by three pieces:

1. **`OutboxManager`** — when a mutation happens while offline, it's added to `SyncOutboxQueries` instead of being sent. When connectivity returns, `SyncEngine` drains it.
2. **`SyncEngine`** — wakes on connectivity change or timer, dispatches each outbox row to the `SyncHandler` registered for that entity type via `SyncEntityRegistry`.
3. **`SyncEntityRegistry`** — an empty registry in the skeleton. Modules call `register(MyEntitySyncHandler(...))` at startup.

`SyncWorker` (Android WorkManager) triggers periodic reconciliation even when the app is backgrounded. iOS uses the app lifecycle hooks from `Main_ios.kt`.

`SyncStateHolder` exposes a `StateFlow<SyncState>` that UI can `collectAsState()` to show pending counts and spinners.

## Observability

Three stubs under `monitoring/`:

- `CrashReporter` — `init(dsn, isDebug)` + `captureException(throwable)`. Default impl is a no-op. Wire to Sentry by replacing `androidMain/.../CrashReporter.android.kt` and `iosMain/.../CrashReporter.ios.kt`.
- `Analytics` — `init(isDebug)` + `track(event, properties)`.
- `PerformanceMonitor` — transactions for important flows.

All three are called from `MobileApplication.onCreate` / `initKoinIOS`. Leaving them as no-ops is fine for dev.

## Push notifications

`PushNotificationManager` (expect/actual) handles registration + token caching. It's initialized in `MobileApplication` but Firebase Messaging is disabled by default. To enable:

1. Drop `google-services.json` into `android/`.
2. Uncomment the `google-services` plugin and `firebase-messaging` dep in `android/build.gradle.kts`.
3. Uncomment the `<service>` entry for `BackboneFirebaseMessagingService` in `AndroidManifest.xml` — and restore the service file (deleted in the skeleton). A minimal implementation is:
   ```kotlin
   class BackboneFirebaseMessagingService : FirebaseMessagingService() {
       override fun onNewToken(token: String) {
           PushNotificationManager.onTokenRefreshed(token)
       }
       override fun onMessageReceived(message: RemoteMessage) {
           PushNotificationManager.onMessageReceived(
               title = message.notification?.title.orEmpty(),
               body = message.notification?.body.orEmpty(),
               data = message.data,
           )
       }
   }
   ```
4. Wire `PushNotificationManager.tokenProvider` in `MobileApplication` to resolve the FCM token.

`PushTokenApiClient` posts the token to your backend. Its registration already lives in `networkModule`.

## Feature flags

`FeatureFlagManager` reads values from `KeyValueStorage` (for overrides) falling back to `RemoteConfigProvider` (for server-driven flags). The skeleton binds `NoOpRemoteConfigProvider` — replace with Firebase Remote Config, LaunchDarkly, or your own once you need real flags.

```kotlin
val flags: FeatureFlagManager = koinInject()
if (flags.isEnabled(FeatureFlag.NEW_CHECKOUT)) { … }
```

Define flags in [`FeatureFlag.kt`](../shared/src/commonMain/kotlin/id/startapp/infrastructure/featureflags/FeatureFlag.kt).

## Media

`MediaCacheManager` stores binary blobs keyed by URL in SQLite + filesystem. Use it for images/files the user needs offline. Eviction is LRU with a size cap.

## Events

`AppEventBus` is a Coroutines `SharedFlow` for cross-cutting app events (auth session invalidated, global snackbar requests). Keep it small — if a flow becomes module-specific, move it out.

## Crypto

`CryptoProvider` (expect/actual) exposes platform keystores. `CacheEncryptor` uses it to wrap sensitive fields before they land in `CacheDao`. By default, only auth-related data is encrypted — modules opt in per-entity.
