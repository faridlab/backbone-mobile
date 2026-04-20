package id.startapp.pheromone.domain.auth.entity

/**
 * Domain model for email verification result.
 */
data class VerifyEmailResult(
    val userId: String?,
    val emailVerified: Boolean
)
