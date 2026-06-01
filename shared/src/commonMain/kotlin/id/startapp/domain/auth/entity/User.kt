package id.startapp.domain.auth.entity

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * User entity representing an authenticated user.
 *
 * @property id Unique user identifier
 * @property username User's username
 * @property email User's email address
 * @property fullName User's full name (optional)
 * @property avatarUrl URL to user's avatar (optional)
 * @property role User's role/permissions
 * @property isActive Whether the user account is active
 * @property emailVerified Whether the email has been verified
 * @property createdAt Timestamp when the account was created
 * @property updatedAt Timestamp when the account was last updated
 */
@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String? = null,
    val avatarUrl: String? = null,
    val role: UserRole = UserRole.USER,
    val isActive: Boolean = true,
    val emailVerified: Boolean = false,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
) {

    /**
     * Get the display name (fullName or username).
     */
    val displayName: String
        get() = fullName ?: username

    /**
     * Get initials for avatar placeholder.
     */
    val initials: String
        get() = displayName
            .split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()

    /**
     * Check if the user has admin role.
     */
    val isAdmin: Boolean
        get() = role == UserRole.ADMIN

    /**
     * Check if the user is verified.
     */
    val isVerified: Boolean
        get() = emailVerified && isActive
}

/**
 * User roles/permissions enum.
 */
@Serializable
enum class UserRole {
    /**
     * Regular user with standard permissions.
     */
    USER,

    /**
     * Moderator with elevated permissions.
     */
    MODERATOR,

    /**
     * Administrator with full permissions.
     */
    ADMIN,

    /**
     * Super user with all permissions including system management.
     */
    SUPER_ADMIN
}
