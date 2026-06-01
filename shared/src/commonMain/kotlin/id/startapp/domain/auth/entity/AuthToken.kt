package id.startapp.domain.auth.entity

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Authentication token entity containing JWT access and refresh tokens.
 *
 * @property accessToken The JWT access token used for API requests
 * @property refreshToken The JWT refresh token used to obtain new access tokens
 * @property expiresIn Access token expiration time in seconds
 * @property issuedAt Timestamp when the token was issued
 * @property tokenType The type of token (typically "Bearer")
 */
data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val issuedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val tokenType: String = "Bearer"
) {

    /**
     * Calculate when the access token expires.
     */
    val expiresAt: Instant
        get() = Instant.fromEpochMilliseconds((issuedAt + expiresIn * 1000))

    /**
     * Check if the access token is expired.
     */
    fun isExpired(): Boolean {
        return Clock.System.now() > expiresAt
    }

    /**
     * Check if the access token will expire within the given seconds.
     *
     * @param withinSeconds Seconds before expiration to consider "soon" (default: 5 minutes)
     * @return true if token will expire within the specified window
     */
    fun isExpiringSoon(withinSeconds: Long = DEFAULT_EXPIRATION_WINDOW_SECONDS): Boolean {
        val now = Clock.System.now()
        val expirationWindow = expiresAt.minus(withinSeconds.toDuration(DurationUnit.SECONDS))
        return now > expirationWindow
    }

    /**
     * Check if the token is valid (not expired).
     */
    val isValid: Boolean
        get() = !isExpired() && accessToken.isNotEmpty()

    companion object {
        /**
         * Default expiration warning window: 5 minutes.
         * Tokens expiring within this window should be refreshed.
         */
        const val DEFAULT_EXPIRATION_WINDOW_SECONDS = 300L

        /**
         * Milliseconds per second for timestamp calculations.
         */
        private const val MILLIS_PER_SECOND = 1000L

        /**
         * Create an AuthToken from a login response.
         */
        fun fromResponse(
            accessToken: String,
            refreshToken: String,
            expiresIn: Long,
            tokenType: String = "Bearer"
        ): AuthToken {
            return AuthToken(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = expiresIn,
                tokenType = tokenType
            )
        }
    }
}
