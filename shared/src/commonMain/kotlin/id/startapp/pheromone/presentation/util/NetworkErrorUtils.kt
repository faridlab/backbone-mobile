package id.startapp.pheromone.presentation.util

import id.startapp.pheromone.domain.types.NetworkError

/**
 * Shared extension to extract a user-friendly message from NetworkError.
 *
 * Prioritises structured validation messages, then falls back to the
 * generic error message.
 */
fun NetworkError.userMessage(): String = when (this) {
    is NetworkError.ValidationError -> {
        errors["message"] ?: errors["general"] ?: firstFieldError ?: message
    }
    else -> message ?: "Terjadi kesalahan"
}
