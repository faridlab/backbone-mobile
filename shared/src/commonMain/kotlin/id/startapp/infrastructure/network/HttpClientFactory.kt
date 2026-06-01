package id.startapp.infrastructure.network
import kotlin.concurrent.Volatile

import id.startapp.domain.types.NetworkError
import id.startapp.domain.types.Result
import id.startapp.infrastructure.events.AppEvent
import id.startapp.infrastructure.events.AppEventBus
import id.startapp.infrastructure.storage.TokenStorage
import kotlin.coroutines.cancellation.CancellationException
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.plugin
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory for creating configured Ktor HTTP clients.
 */
object HttpClientFactory {

    /**
     * Create a new HTTP client with default configuration.
     *
     * @param tokenStorage Optional token storage for authenticated requests
     * @param tokenRefreshProvider Optional provider for transparent 401 token refresh
     * @return Configured HttpClient
     */
    /** Device identity for multi-device sync headers. Atomically set via [setDeviceIdentity]. */
    @Volatile
    private var deviceIdentity: Pair<String, String?>? = null

    /**
     * Set the device ID and name for X-Device-Id / X-Device-Name headers.
     * Call once during app initialization after Koin resolves KeyValueStorage.
     */
    fun setDeviceIdentity(id: String, name: String? = null) {
        deviceIdentity = id to name
    }

    fun create(
        tokenStorage: TokenStorage? = null,
        tokenRefreshProvider: TokenRefreshProvider? = null,
        appEventBus: AppEventBus? = null,
    ): HttpClient {
        return createBaseClient().also { client ->
            if (tokenStorage != null) {
                installAuthInterceptor(client, tokenStorage, tokenRefreshProvider, appEventBus)
            }
        }
    }

    /**
     * Create an HTTP client for authenticated requests.
     *
     * @param tokenStorage Token storage for injecting auth headers
     * @param tokenRefreshProvider Optional provider for transparent 401 token refresh
     * @return Configured HttpClient with auth interceptor
     */
    fun createAuthenticated(
        tokenStorage: TokenStorage,
        tokenRefreshProvider: TokenRefreshProvider? = null,
        appEventBus: AppEventBus? = null,
    ): HttpClient {
        return createBaseClient().also { client ->
            installAuthInterceptor(client, tokenStorage, tokenRefreshProvider, appEventBus)
        }
    }

    /**
     * Create a base HttpClient with common plugins (JSON, timeouts, logging).
     */
    private fun createBaseClient(): HttpClient {
        return HttpClient {
            installJsonSerialization()
            installTimeouts()
            installLogging()
            installResponseValidation()
        }
    }

    /**
     * Install JSON serialization configuration.
     */
    private fun io.ktor.client.HttpClientConfig<*>.installJsonSerialization() {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    prettyPrint = false
                    isLenient = true
                }
            )
        }
    }

    /**
     * Install timeout configuration.
     *
     * Default values: Request: 30s, Connect: 10s, Socket: 30s
     */
    private fun io.ktor.client.HttpClientConfig<*>.installTimeouts() {
        install(HttpTimeout) {
            requestTimeoutMillis = BuildConfig.REQUEST_TIMEOUT_MS
            connectTimeoutMillis = BuildConfig.CONNECT_TIMEOUT_MS
            socketTimeoutMillis = BuildConfig.SOCKET_TIMEOUT_MS
        }
    }

    /**
     * Install logging based on build type.
     *
     * Debug builds: Full logging (ALL)
     * Release builds: No logging (NONE) for security
     */
    private fun io.ktor.client.HttpClientConfig<*>.installLogging() {
        install(Logging) {
            level = if (BuildConfig.IS_DEBUG) {
                LogLevel.ALL
            } else {
                LogLevel.NONE
            }
            logger = Logger.SIMPLE
        }
    }

    /**
     * Install response validation to convert non-2xx HTTP responses to NetworkError
     * before Ktor's content negotiation attempts to deserialize the body.
     */
    private fun io.ktor.client.HttpClientConfig<*>.installResponseValidation() {
        HttpResponseValidator {
            validateResponse { response ->
                if (!response.status.isSuccess()) {
                    throw handleApiError(response)
                }
            }
        }
    }

    /**
     * Install auth interceptor on an existing HttpClient.
     *
     * Uses HttpSend plugin to:
     * 1. Read the Bearer token from TokenStorage before EVERY request
     * 2. On 401: attempt token refresh (if provider available), then retry once
     * 3. Clear stored tokens if refresh fails or no provider
     */
    private fun installAuthInterceptor(
        client: HttpClient,
        tokenStorage: TokenStorage,
        tokenRefreshProvider: TokenRefreshProvider?,
        appEventBus: AppEventBus? = null,
    ) {
        client.plugin(HttpSend).intercept { request ->
            val token = tokenStorage.getAccessToken()
            if (token != null && !request.headers.contains(HttpHeaders.Authorization)) {
                request.headers.append(HttpHeaders.Authorization, "Bearer $token")
            }

            // Attach device identity headers for multi-device sync
            deviceIdentity?.let { (id, name) ->
                if (!request.headers.contains("X-Device-Id")) {
                    request.headers.append("X-Device-Id", id)
                }
                if (name != null && !request.headers.contains("X-Device-Name")) {
                    request.headers.append("X-Device-Name", name)
                }
            }

            val call = execute(request)

            if (call.response.status.value != 401) return@intercept call

            // 401 — attempt refresh if provider available
            if (tokenRefreshProvider != null) {
                when (tokenRefreshProvider.refreshToken()) {
                    is Result.Success -> {
                        // Retry with new token
                        request.headers.remove(HttpHeaders.Authorization)
                        tokenStorage.getAccessToken()?.let { newToken ->
                            request.headers.append(HttpHeaders.Authorization, "Bearer $newToken")
                        }
                        return@intercept execute(request)
                    }
                    is Result.Error -> {
                        try { tokenStorage.clearToken() } catch (e: Exception) { if (e is CancellationException) throw e }
                        appEventBus?.tryEmit(AppEvent.SessionExpired)
                    }
                }
            } else {
                try { tokenStorage.clearToken() } catch (e: Exception) { if (e is CancellationException) throw e }
                appEventBus?.tryEmit(AppEvent.SessionExpired)
            }

            call
        }
    }
}
