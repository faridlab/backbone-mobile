package id.startapp.core.api
import kotlin.concurrent.Volatile

import id.startapp.domain.types.Result
import id.startapp.infrastructure.network.CertificatePinningManager
import id.startapp.infrastructure.network.RequestSigner
import id.startapp.infrastructure.network.RetryConfig
import id.startapp.infrastructure.network.apiCall
import id.startapp.infrastructure.network.calculateBackoff
import id.startapp.infrastructure.network.isRetryable
import id.startapp.infrastructure.network.signRequest
import id.startapp.infrastructure.pagination.BackendPaginatedResponse
import id.startapp.infrastructure.pagination.PaginatedApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

// Generic CRUD base for generated API clients.
//
// Ktor's body() is reified, so it cannot be called with an erased generic type E
// inside this abstract class. Subclasses implement the two abstract deserialize hooks,
// calling body() with the concrete entity type at the subclass level.
//
// Generated subclass example:
//   class OrderApiClient(httpClient: HttpClient, baseUrl: String)
//       : BaseCrudApiClient<Order>(httpClient, baseUrl) {
//       override val basePath = "$baseUrl/api/v1/backbone/orders"
//       override suspend fun deserializeOne(r: HttpResponse): Order = r.body()
//       override suspend fun deserializeList(r: HttpResponse): BackendPaginatedResponse<Order> = r.body()
//   }
abstract class BaseCrudApiClient<E>(
    protected val httpClient: HttpClient,
    protected val baseUrl: String,
) {
    protected abstract val basePath: String

    // Implemented by generated subclass with concrete reified body() call
    protected abstract suspend fun deserializeOne(response: HttpResponse): E
    protected abstract suspend fun deserializeList(response: HttpResponse): BackendPaginatedResponse<E>

    // Expose for extension functions — mirrors AgentTaskApiClient pattern
    internal val client: HttpClient get() = httpClient
    internal val serverUrl: String get() = baseUrl

    // Path component of basePath without the scheme+host prefix.
    // RequestSigner.signRequest() expects the path only (e.g. "/api/v1/orders"),
    // not the full URL. basePath includes the full URL because it is constructed
    // as "$baseUrl/api/v1/…" in each subclass.
    private val basePathSegment: String
        get() = basePath.removePrefix(baseUrl)

    // ── 9A — Certificate pinning guard ───────────────────────────────────────
    //
    // Declare hostnames that MUST have certificate pinning configured in
    // CertificatePinningManager before any request is sent. If a declared host
    // has no pin registered, a warning is printed at construction time.
    //
    // Actual TLS-level pin enforcement is platform-specific and must be wired
    // into the Ktor engine (OkHttp CertificatePinner on Android, URLSession
    // challenge handler on iOS). This guard ensures misconfiguration is caught
    // at startup rather than silently skipped at runtime.
    //
    // Example:
    //   override val pinnedHosts = setOf("api.backbone.com")
    protected open val pinnedHosts: Set<String> = emptySet()

    // Deferred to first request — init {} runs before subclass property initializers,
    // so reading an open val from init {} always returns the superclass default (emptySet()).
    //
    // Uses double-checked locking with a coroutine Mutex to guarantee exactly-once
    // execution even under concurrent requests. The outer @Volatile read is the fast
    // path (no lock once checked); the inner check inside withLock is the correctness
    // guard that prevents a second coroutine from re-running the scan after it waited
    // for the lock while the first coroutine was already running it.
    @Volatile private var pinningChecked = false
    private val pinningCheckMutex = Mutex()

    private suspend fun checkPinningConfigurationOnce() {
        if (pinningChecked) return  // fast path — lock-free after first completion
        pinningCheckMutex.withLock {
            if (pinningChecked) return@withLock  // second coroutine: already done
            pinningChecked = true
            pinnedHosts.forEach { host ->
                if (!CertificatePinningManager.isConfiguredForHost(host)) {
                    println(
                        "W/BaseCrudApiClient: certificate pinning declared for '$host' but no pin " +
                        "is registered in CertificatePinningManager — call " +
                        "CertificatePinningManager.addConfig() during app initialisation."
                    )
                }
            }
        }
    }

    // ── 9B — Request signing (opt-in) ─────────────────────────────────────────
    //
    // Provide a RequestSigner to enable HMAC-SHA256 signing on all CRUD methods.
    // Null (default) skips signing — suitable for endpoints that rely solely on
    // bearer-token authentication. Subclasses or custom clients for high-security
    // endpoints should override this:
    //
    //   override val requestSigner = RequestSigner(apiKey, secretKey)
    protected open val requestSigner: RequestSigner? = null

    // ── 6A — Retry configuration ──────────────────────────────────────────────
    //
    // Override in a generated subclass or custom extension to use a different
    // retry policy (e.g. RetryConfig.noRetry() for mutation-heavy clients).
    protected open val retryConfig: RetryConfig = RetryConfig.default()

    // ── 6B — Request timeout ──────────────────────────────────────────────────
    //
    // Applied per-request via Ktor's timeout {} block.
    // Override to increase for long-running endpoints (e.g. file uploads).
    protected open val requestTimeoutMs: Long = 30_000L

    // ── 8B — Correlation ID ───────────────────────────────────────────────────
    //
    // Generates a unique ID per request that is sent as X-Correlation-ID.
    // Backend logs and Sentry traces can be joined on this value for end-to-end
    // visibility across the mobile→API boundary.
    // Override to use a full UUID library if available in your project.
    protected open fun newCorrelationId(): String =
        "mob-${Clock.System.now().toEpochMilliseconds()}-${(10000..99999).random()}"

    // ── 6A: internal retry wrapper around apiCall ─────────────────────────────

    protected suspend fun <T> retryApiCall(block: suspend () -> Result<T>): Result<T> {
        checkPinningConfigurationOnce()
        for (attempt in 0 until retryConfig.maxAttempts) {
            val result = block()
            if (result is Result.Success) return result
            val error = (result as Result.Error).error
            val isLastAttempt = attempt >= retryConfig.maxAttempts - 1
            if (!isLastAttempt && error.isRetryable()) {
                delay(calculateBackoff(attempt + 1, retryConfig))
            } else {
                return result
            }
        }
        // Unreachable when maxAttempts >= 1, satisfies Kotlin exhaustive return
        return block()
    }

    // ── CRUD methods ──────────────────────────────────────────────────────────

    suspend fun getById(id: String): Result<E> {
        val correlationId = newCorrelationId()
        return retryApiCall {
            apiCall {
                deserializeOne(
                    httpClient.get("$basePath/$id") {
                        header("X-Correlation-ID", correlationId)
                        requestSigner?.let { signRequest(it, "GET", "$basePathSegment/$id") }
                        timeout { requestTimeoutMillis = requestTimeoutMs }
                    }
                )
            }
        }
    }

    suspend fun getAll(
        page: Int = 1,
        limit: Int = 20,
        sortBy: String = "created_at",
        sortDesc: Boolean = true,
    ): Result<PaginatedApiResponse<E>> {
        val correlationId = newCorrelationId()
        // Canonical query string must be built before the request for signing
        val sortParam = if (sortDesc) "$sortBy:desc" else "$sortBy:asc"
        val queryString = "page=$page&limit=$limit&sort=$sortParam"
        return retryApiCall {
            apiCall {
                val response = deserializeList(
                    httpClient.get(basePath) {
                        header("X-Correlation-ID", correlationId)
                        requestSigner?.let { signRequest(it, "GET", basePathSegment, queryString) }
                        timeout { requestTimeoutMillis = requestTimeoutMs }
                        parameter("page", page)
                        parameter("limit", limit)
                        parameter("sort", sortParam)
                    }
                )
                response.toPaginatedApiResponse()
            }
        }
    }

    suspend fun delete(id: String): Result<Unit> {
        val correlationId = newCorrelationId()
        return retryApiCall {
            apiCall {
                httpClient.delete("$basePath/$id") {
                    header("X-Correlation-ID", correlationId)
                    requestSigner?.let { signRequest(it, "DELETE", "$basePathSegment/$id") }
                    timeout { requestTimeoutMillis = requestTimeoutMs }
                }.body()
            }
        }
    }
}
