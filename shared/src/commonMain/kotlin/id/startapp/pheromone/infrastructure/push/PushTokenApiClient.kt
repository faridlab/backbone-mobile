package id.startapp.pheromone.infrastructure.push

import id.startapp.pheromone.domain.types.NetworkError
import id.startapp.pheromone.domain.types.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable

/**
 * API client for registering and unregistering push tokens with the backend.
 *
 * @param httpClient Authenticated HTTP client
 * @param baseUrl API base URL
 */
class PushTokenApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {

    /**
     * Register a push token with the backend.
     *
     * @param token The push token to register
     * @param platform Platform identifier ("android" or "ios")
     * @return Success or error result
     */
    suspend fun registerToken(token: String, platform: String): Result<Unit> {
        return try {
            val response = httpClient.post("$baseUrl/api/v1/push-tokens") {
                contentType(ContentType.Application.Json)
                setBody(PushTokenRequest(token = token, platform = platform))
            }
            if (response.status.isSuccess()) {
                Result.Success(Unit)
            } else {
                Result.Error(NetworkError.ServerError(response.status.value, response.bodyAsText()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(NetworkError.UnknownError(e))
        }
    }

    /**
     * Unregister a push token from the backend.
     *
     * @param token The push token to unregister
     * @return Success or error result
     */
    suspend fun unregisterToken(token: String): Result<Unit> {
        return try {
            val response = httpClient.delete("$baseUrl/api/v1/push-tokens/${token.encodeURLPath()}")
            if (response.status.isSuccess()) {
                Result.Success(Unit)
            } else {
                Result.Error(NetworkError.ServerError(response.status.value, response.bodyAsText()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(NetworkError.UnknownError(e))
        }
    }
}

@Serializable
private data class PushTokenRequest(
    val token: String,
    val platform: String
)
