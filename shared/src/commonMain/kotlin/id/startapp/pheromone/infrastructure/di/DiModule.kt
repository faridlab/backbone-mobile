package id.startapp.pheromone.infrastructure.di

import id.startapp.pheromone.application.usecases.auth.GetCurrentUserUseCase
import id.startapp.pheromone.application.usecases.auth.LoginUseCase
import id.startapp.pheromone.application.usecases.auth.LogoutUseCase
import id.startapp.pheromone.application.usecases.auth.RefreshTokenUseCase
import id.startapp.pheromone.application.usecases.auth.RegisterUseCase
import id.startapp.pheromone.application.usecases.auth.RequestPasswordResetUseCase
import id.startapp.pheromone.application.usecases.auth.ResendVerificationUseCase
import id.startapp.pheromone.application.usecases.auth.ResetPasswordUseCase
import id.startapp.pheromone.application.usecases.auth.VerifyEmailUseCase
import id.startapp.pheromone.domain.auth.repository.AuthRepository
import id.startapp.pheromone.domain.demo.repository.TodoRepository
import id.startapp.pheromone.infrastructure.auth.BiometricAuthManager
import id.startapp.pheromone.infrastructure.crypto.CacheEncryptor
import id.startapp.pheromone.infrastructure.crypto.CryptoProvider
import id.startapp.pheromone.infrastructure.database.dao.CacheDao
import id.startapp.pheromone.infrastructure.database.dao.ConflictDao
import id.startapp.pheromone.infrastructure.database.dao.MediaCacheDao
import id.startapp.pheromone.infrastructure.database.dao.OutboxDao
import id.startapp.pheromone.infrastructure.database.dao.SyncHistoryDao
import id.startapp.pheromone.infrastructure.database.dao.TodoDao
import id.startapp.pheromone.infrastructure.events.AppEventBus
import id.startapp.pheromone.infrastructure.featureflags.FeatureFlagManager
import id.startapp.pheromone.infrastructure.featureflags.NoOpRemoteConfigProvider
import id.startapp.pheromone.infrastructure.featureflags.RemoteConfigProvider
import id.startapp.pheromone.infrastructure.media.MediaCacheManager
import id.startapp.pheromone.infrastructure.network.BuildConfig
import id.startapp.pheromone.infrastructure.network.DefaultTokenRefreshProvider
import id.startapp.pheromone.infrastructure.network.HttpClientFactory
import id.startapp.pheromone.infrastructure.network.InMemoryCache
import id.startapp.pheromone.infrastructure.network.TokenRefreshProvider
import id.startapp.pheromone.infrastructure.network.api.AuthApi
import id.startapp.pheromone.infrastructure.network.api.AuthApiClient
import id.startapp.pheromone.infrastructure.push.PushTokenApiClient
import id.startapp.pheromone.infrastructure.repository.AuthRepositoryImpl
import id.startapp.pheromone.infrastructure.repository.TodoRepositoryImpl
import id.startapp.pheromone.infrastructure.repository.UserProfileCache
import id.startapp.pheromone.infrastructure.sync.OutboxManager
import id.startapp.pheromone.infrastructure.sync.SyncEngine
import id.startapp.pheromone.infrastructure.sync.SyncEntityRegistry
import id.startapp.pheromone.infrastructure.sync.SyncStateHolder
import id.startapp.pheromone.infrastructure.sync.SyncWorker
import id.startapp.pheromone.infrastructure.sync.VersionTracker
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin entry point. Platform-specific bits (SqlDriver, DataStore) come from [platformModule].
 */
fun initKoin(config: KoinAppDeclaration = {}) {
    startKoin {
        config()
        modules(
            platformModule,
            storageModule,
            databaseModule,
            networkModule,
            repositoryModule,
            useCaseModule,
        )
    }
}

expect val platformModule: org.koin.core.module.Module

val storageModule = module {
    single<RemoteConfigProvider> { NoOpRemoteConfigProvider() }
    single { FeatureFlagManager(storage = get(), remoteConfigProvider = get(), isDebug = BuildConfig.IS_DEBUG) }
}

val databaseModule = module {
    single { TodoDao() }
    single { CacheDao() }
    single { OutboxDao() }
    single { ConflictDao() }
    single { SyncHistoryDao() }
    single { MediaCacheDao() }
    single { SyncStateHolder(connectivityMonitor = get(), outboxDao = get(), conflictDao = get()) }
    single { OutboxManager(outboxDao = get(), connectivityMonitor = get(), keyValueStorage = get(), appEventBus = get(), syncStateHolder = get()) }
    single { SyncEntityRegistry() }
    single { SyncEngine(outboxManager = get(), conflictDao = get(), syncHistoryDao = get(), entityRegistry = get(), keyValueStorage = get(), syncStateHolder = get(), appEventBus = get()) }
    single { SyncWorker(cacheDao = get(), syncEngine = get()) }
    single { VersionTracker(cacheDao = get()) }
    single { CryptoProvider() }
    single { CacheEncryptor(cryptoProvider = get()) }
    single { BiometricAuthManager() }
    single { MediaCacheManager(mediaCacheDao = get(), keyValueStorage = get()) }
}

val networkModule = module {
    single { HttpClientFactory }
    single { InMemoryCache() }
    single { AppEventBus() }

    single<HttpClient>(named("unauthenticated")) { HttpClientFactory.create() }

    single<AuthApiClient>(named("refreshClient")) {
        AuthApiClient(httpClient = get(named("unauthenticated")))
    }
    single<TokenRefreshProvider> {
        DefaultTokenRefreshProvider(
            refreshApiClient = get(named("refreshClient")),
            tokenStorage = get(),
        )
    }

    single<HttpClient>(named("authenticated")) {
        HttpClientFactory.create(
            tokenStorage = get(),
            tokenRefreshProvider = get(),
            appEventBus = get(),
        )
    }

    single { AuthApiClient(httpClient = get(named("authenticated"))) } bind AuthApi::class
    single { PushTokenApiClient(httpClient = get(named("authenticated")), baseUrl = BuildConfig.API_BASE_URL) }
}

val repositoryModule = module {
    single { UserProfileCache(storage = get()) }

    single<AuthRepository> {
        AuthRepositoryImpl(
            authApiClient = get<AuthApi>(),
            tokenStorage = get(),
            profileCache = get(),
        )
    }

    single<TodoRepository> { TodoRepositoryImpl(todoDao = get()) }
}

val useCaseModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::RefreshTokenUseCase)
    factoryOf(::GetCurrentUserUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::VerifyEmailUseCase)
    factoryOf(::ResendVerificationUseCase)
    factoryOf(::RequestPasswordResetUseCase)
    factoryOf(::ResetPasswordUseCase)
}
