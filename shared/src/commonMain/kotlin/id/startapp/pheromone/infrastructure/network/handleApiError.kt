package id.startapp.pheromone.infrastructure.network

import id.startapp.pheromone.domain.types.NetworkError
import id.startapp.pheromone.infrastructure.monitoring.CrashReporter
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException

/**
 * Convert HTTP response errors to NetworkError.
 *
 * @param response The HTTP response
 * @return NetworkError instance
 */
suspend fun handleApiError(response: HttpResponse): NetworkError {
    return when (response.status) {
        HttpStatusCode.Unauthorized -> NetworkError.Unauthorized(
            errorBody = response.bodyOrNull()
        )
        HttpStatusCode.Forbidden -> NetworkError.Forbidden(
            errorBody = response.bodyOrNull()
        )
        HttpStatusCode.NotFound -> NetworkError.NotFound(
            resource = response.call.request.url.encodedPath
        )
        HttpStatusCode.Conflict -> NetworkError.ConflictError(
            errorBody = response.bodyOrNull()
        )
        HttpStatusCode.BadRequest,
        HttpStatusCode.UnprocessableEntity -> {
            val errorBody = response.bodyOrNull()
            NetworkError.ValidationError(
                errors = parseValidationErrors(errorBody)
            )
        }
        else -> when (val status = response.status.value) {
            in 500..599 -> NetworkError.ServerError(
                statusCode = status,
                errorBody = response.bodyOrNull()
            )
            else -> NetworkError.UnknownError()
        }
    }
}

/**
 * Get response body as text if available.
 */
private suspend inline fun HttpResponse.bodyOrNull(): String? {
    return try {
        body<String>()
    } catch (e: Exception) {
        null
    }
}

/**
 * Parse validation errors from error body.
 *
 * Supports common API error response formats:
 * 1. JSON with "errors" object: {"errors": {"email": "Invalid email", "password": "Too short"}}
 * 2. JSON with "message" string: {"message": "Validation failed"}
 * 3. JSON with field-specific errors: {"email": ["Invalid email"], "password": ["Too short"]}
 * 4. Plain text fallback
 */
private fun parseValidationErrors(errorBody: String?): Map<String, String> {
    if (errorBody.isNullOrBlank()) return emptyMap()

    // Try to parse as JSON
    return try {
        parseJsonErrors(errorBody)
    } catch (e: Exception) {
        // Fallback to generic error if parsing fails
        mapOf("general" to errorBody)
    }
}

/**
 * Parse validation errors from JSON response.
 */
private fun parseJsonErrors(body: String): Map<String, String> {
    val result = mutableMapOf<String, String>()
    val trimmedBody = body.trim()

    // Pattern 1: {"errors": {"field": "message"}}
    if (trimmedBody.contains("\"errors\"")) {
        val errorsMatch = Regex("\"errors\"\\s*:\\s*\\{([^}]+)\\}").find(trimmedBody)
        errorsMatch?.groupValues?.get(1)?.let { errorsContent ->
            parseFieldErrors(errorsContent, result)
        }
    }

    // Pattern 2: {"message": "error text"} — prioritize over generic field extraction
    // This handles backend error responses like {"error_code":"...","message":"Human-readable text"}
    if (result.isEmpty() && trimmedBody.contains("\"message\"")) {
        val messageMatch = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(trimmedBody)
        messageMatch?.groupValues?.get(1)?.let {
            result["message"] = it
        }
    }

    // Pattern 3: {"field": "message"} or {"field": ["message"]}
    if (result.isEmpty()) {
        parseFieldErrors(trimmedBody.dropWhile { it != '{' }.drop(1), result)
    }

    // Fallback: use entire body as general error
    if (result.isEmpty()) {
        result["general"] = body
    }

    return result
}

/**
 * Parse field errors from JSON content.
 * Supports: "field": "message" and "field": ["message1", "message2"]
 */
private fun parseFieldErrors(content: String, result: MutableMap<String, String>) {
    // Match "field": "value" pattern
    val fieldPattern = Regex("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"")
    fieldPattern.findAll(content).forEach { match ->
        result[match.groupValues[1]] = match.groupValues[2]
    }

    // Match "field": ["value"] pattern
    val arrayPattern = Regex("\"([^\"]+)\"\\s*:\\s*\\[\"([^\"]+)\"\\]")
    arrayPattern.findAll(content).forEach { match ->
        result[match.groupValues[1]] = match.groupValues[2]
    }

    // Match nested errors: "field": {"message": "text"}
    val nestedPattern = Regex("\"([^\"]+)\"\\s*:\\s*\\{[^}]*\"message\"\\s*:\\s*\"([^\"]+)\"")
    nestedPattern.findAll(content).forEach { match ->
        result[match.groupValues[1]] = match.groupValues[2]
    }
}

/**
 * Wrap a suspending API call in a Result type.
 *
 * @param block The API call to execute
 * @return Result with data or NetworkError
 */
suspend inline fun <T> apiCall(
    crossinline block: suspend () -> T
): id.startapp.pheromone.domain.types.Result<T> = try {
    val data = block()
    id.startapp.pheromone.domain.types.Result.Success(data)
} catch (e: IOException) {
    CrashReporter.addBreadcrumb("network", "Connectivity error", mapOf("error" to (e.message ?: "unknown")))
    id.startapp.pheromone.domain.types.Result.Error(NetworkError.ConnectivityError(e))
} catch (e: kotlinx.serialization.SerializationException) {
    CrashReporter.captureException(e)
    id.startapp.pheromone.domain.types.Result.Error(
        NetworkError.ValidationError(mapOf("general" to "Response parsing error: ${e.message}"))
    )
} catch (e: NetworkError) {
    id.startapp.pheromone.domain.types.Result.Error(e)
} catch (e: Exception) {
    CrashReporter.captureException(e)
    id.startapp.pheromone.domain.types.Result.Error(NetworkError.UnknownError(e))
}
