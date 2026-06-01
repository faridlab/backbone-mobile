package id.startapp.infrastructure.network

/**
 * API configuration.
 *
 * Contains base URLs and endpoints for the backend API.
 * Uses platform-specific BuildConfig for configurable values.
 */
object ApiConfig {

    /**
     * Base URL for the API.
     * Configured via BuildConfig (platform-specific).
     */
    val BASE_URL: String get() = BuildConfig.API_BASE_URL

    /**
     * API version prefix.
     */
    private const val API_V1 = "/api/v1"

    /**
     * Full base URL with version.
     */
    val baseUrl: String get() = "$BASE_URL$API_V1"

    /**
     * Auth endpoints.
     */
    object Auth {
        val LOGIN: String get() = "$baseUrl/auth/login"
        val REGISTER: String get() = "$baseUrl/auth/register"
        val REFRESH: String get() = "$baseUrl/auth/refresh"
        val LOGOUT: String get() = "$baseUrl/auth/logout"
        val ME: String get() = "$baseUrl/auth/me"
        val FORGOT_PASSWORD: String get() = "$baseUrl/auth/forgot-password"
        val RESET_PASSWORD: String get() = "$baseUrl/auth/reset-password"
        val VERIFY_EMAIL: String get() = "$baseUrl/auth/verify-email"
        val RESEND_VERIFICATION: String get() = "$baseUrl/auth/resend-verification"
        val USER_PROVIDER: String get() = "$baseUrl/auth/user/provider"
    }
}
