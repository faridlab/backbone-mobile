package id.startapp.pheromone.infrastructure.network.api

import id.startapp.pheromone.domain.auth.entity.AuthToken
import id.startapp.pheromone.domain.auth.entity.RegisterResult
import id.startapp.pheromone.domain.auth.entity.User
import id.startapp.pheromone.domain.auth.entity.UserRole
import id.startapp.pheromone.domain.auth.entity.VerifyEmailResult
import id.startapp.pheromone.domain.types.NetworkError
import id.startapp.pheromone.domain.types.Result
import id.startapp.pheromone.infrastructure.network.ApiConfig
import id.startapp.pheromone.infrastructure.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API client for authentication operations.
 */
class AuthApiClient(
    private val httpClient: HttpClient
) : AuthApi {

    /**
     * Login with email and password.
     *
     * @param email User's email
     * @param password User's password
     * @return Result containing AuthToken on success
     */
    override suspend fun login(email: String, password: String): Result<AuthToken> {
        return apiCall {
            val response = httpClient.post(ApiConfig.Auth.LOGIN) {
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("email", email)
                            append("password", password)
                        }
                    )
                )
            }
            // Try to parse as LoginResponse first (success case)
            val loginResponse = try {
                response.body<LoginResponse>()
            } catch (e: Exception) {
                // If that fails, try to parse as error response
                val errorResponse = response.body<LoginErrorResponse>()
                if (!errorResponse.success) {
                    val errorMessage = errorResponse.error?.message
                        ?: errorResponse.message
                        ?: "Invalid email or password"
                    throw NetworkError.ValidationError(mapOf("general" to errorMessage))
                }
                throw NetworkError.UnknownError(e)
            }
            loginResponse.toAuthToken()
        }
    }

    /**
     * Register a new user.
     *
     * Sends JSON body matching backend RegistrationRequestPayload.
     * Backend automatically sends verification email on success.
     * Username (first_name) is auto-derived from the email prefix.
     *
     * @param email User's email (also used as username)
     * @param password User's password
     * @param confirmPassword Password confirmation
     * @return Result containing RegisterResult on success
     */
    override suspend fun register(
        email: String,
        password: String,
        confirmPassword: String
    ): Result<RegisterResult> {
        val username = email.substringBefore("@")
        return apiCall {
            val response = httpClient.post(ApiConfig.Auth.REGISTER) {
                contentType(ContentType.Application.Json)
                setBody(
                    RegisterRequestDto(
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword,
                        firstName = username,
                        lastName = username,
                        acceptTerms = true
                    )
                )
            }
            val wrapper = response.body<ApiResponseWrapper<RegisterResponseDto>>()
            if (wrapper.success && wrapper.data != null && wrapper.data.success) {
                wrapper.data.toDomain()
            } else {
                val errorMessage = wrapper.extractRegisterErrorMessage("Registration failed")
                throw NetworkError.ValidationError(mapOf("general" to errorMessage))
            }
        }
    }

    /**
     * Verify email with OTP token.
     *
     * @param email User's email address
     * @param verificationToken The 6-digit OTP code
     * @return Result containing VerifyEmailResult on success
     */
    override suspend fun verifyEmail(
        email: String,
        verificationToken: String
    ): Result<VerifyEmailResult> {
        return apiCall {
            val response = httpClient.post(ApiConfig.Auth.VERIFY_EMAIL) {
                contentType(ContentType.Application.Json)
                setBody(
                    VerifyEmailRequestDto(
                        email = email,
                        verificationToken = verificationToken
                    )
                )
            }
            val wrapper = response.body<ApiResponseWrapper<VerifyEmailResponseDto>>()
            if (wrapper.success && wrapper.data != null && wrapper.data.success) {
                wrapper.data.toDomain()
            } else {
                val errorMessage = wrapper.extractErrorMessageWithData("Email verification failed")
                throw NetworkError.ValidationError(mapOf("general" to errorMessage))
            }
        }
    }

    /**
     * Resend verification email.
     *
     * @param email User's email address
     * @return Result indicating success or failure
     */
    override suspend fun resendVerification(email: String): Result<Unit> {
        return apiCall {
            val response = httpClient.post(ApiConfig.Auth.RESEND_VERIFICATION) {
                contentType(ContentType.Application.Json)
                setBody(
                    ResendVerificationRequestDto(email = email)
                )
            }
            val wrapper = response.body<ApiResponseWrapper<ResendVerificationResponseDto>>()
            if (wrapper.success && wrapper.data != null && wrapper.data.success) {
                Unit
            } else {
                val errorMessage = wrapper.extractErrorMessageWithData("Failed to resend verification email")
                throw NetworkError.ValidationError(mapOf("general" to errorMessage))
            }
        }
    }

    /**
     * Refresh the access token.
     *
     * @param refreshToken The refresh token
     * @return Result containing AuthToken on success
     */
    override suspend fun refreshToken(refreshToken: String): Result<AuthToken> {
        return apiCall {
            val response = httpClient.post(ApiConfig.Auth.REFRESH) {
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("refresh_token", refreshToken)
                        }
                    )
                )
            }
            response.body<LoginResponse>().toAuthToken()
        }
    }

    /**
     * Get the current authenticated user.
     *
     * @return Result containing User on success
     */
    override suspend fun getCurrentUser(): Result<User> {
        return apiCall {
            val response = httpClient.get(ApiConfig.Auth.ME)
            response.body<UserResponse>().toDomain()
        }
    }

    /**
     * Check if the authenticated user has a provider.
     *
     * Returns user provider info if exists, null otherwise.
     * Null means user needs to complete owner onboarding.
     *
     * @return Result containing UserProviderResponse? (null = no provider)
     */
    override suspend fun getUserProvider(): Result<UserProviderResponse?> {
        return apiCall {
            val response = httpClient.get(ApiConfig.Auth.USER_PROVIDER)
            response.body<ApiResponseWrapper<UserProviderResponseDto?>>()
        }.map { wrapper ->
            wrapper.data?.toDomain()
        }
    }

    /**
     * Logout the current user.
     *
     * @return Result indicating success or failure
     */
    override suspend fun logout(): Result<Unit> {
        return apiCall {
            httpClient.post(ApiConfig.Auth.LOGOUT)
            Unit
        }
    }

    /**
     * Request a password reset OTP email.
     *
     * Backend always returns success to prevent email enumeration.
     *
     * @param email User's email address
     * @return Result indicating success or failure
     */
    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        return apiCall {
            val response = httpClient.post(ApiConfig.Auth.FORGOT_PASSWORD) {
                contentType(ContentType.Application.Json)
                setBody(ForgotPasswordRequestDto(email = email))
            }
            val wrapper = response.body<ApiResponseWrapper<ForgotPasswordResponseDto>>()
            if (wrapper.success && wrapper.data != null && wrapper.data.success) {
                Unit
            } else {
                val errorMessage = wrapper.extractErrorMessageWithData("Failed to send reset email")
                throw NetworkError.ValidationError(mapOf("general" to errorMessage))
            }
        }
    }

    /**
     * Reset password with OTP token and new password.
     *
     * @param token The 6-digit OTP code from email
     * @param newPassword The new password
     * @param confirmPassword Password confirmation
     * @return Result indicating success or failure
     */
    override suspend fun resetPassword(
        token: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> {
        return apiCall {
            val response = httpClient.post(ApiConfig.Auth.RESET_PASSWORD) {
                contentType(ContentType.Application.Json)
                setBody(
                    ResetPasswordRequestDto(
                        token = token,
                        newPassword = newPassword,
                        confirmPassword = confirmPassword
                    )
                )
            }
            val wrapper = response.body<ApiResponseWrapper<ResetPasswordResponseDto>>()
            if (wrapper.success && wrapper.data != null && wrapper.data.success) {
                Unit
            } else {
                val errorMessage = wrapper.extractErrorMessageWithData("Password reset failed")
                throw NetworkError.ValidationError(mapOf("general" to errorMessage))
            }
        }
    }
}

// ── Backend API Response Wrapper ─────────────────────────────────────────────

/**
 * Common interface for response DTOs that have a message field.
 * Allows extraction of error messages from typed response data.
 */
internal interface MessageResponse {
    val message: String
}

/**
 * Generic wrapper for backend ApiResponse<T> envelope.
 * Backend wraps all responses in {success, data, error, timestamp}.
 */
@Serializable
internal data class ApiResponseWrapper<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiErrorWrapper? = null,
    val timestamp: String? = null
)

@Serializable
internal data class ApiErrorWrapper(
    val code: String? = null,
    val message: String? = null
)

// ── Extension Functions ────────────────────────────────────────────────────────

/**
 * Extract error message from ApiResponseWrapper.
 * Provides a consistent fallback pattern for error handling.
 *
 * @param default The default message if no error message is found
 * @return The error message to display to the user
 */
private fun <T> ApiResponseWrapper<T>.extractErrorMessage(default: String): String {
    return error?.message ?: default
}

/**
 * Extract error message from ApiResponseWrapper with message response support.
 * Used when the data type implements MessageResponse interface.
 *
 * @param default The default message if no error message is found
 * @return The error message to display to the user
 */
private fun <T : MessageResponse> ApiResponseWrapper<T>.extractErrorMessageWithData(default: String): String {
    return data?.message?.takeIf { it.isNotEmpty() } ?: error?.message ?: default
}

/**
 * Extract error message specifically from RegisterResponseDto which has errors array.
 */
private fun ApiResponseWrapper<RegisterResponseDto>.extractRegisterErrorMessage(default: String): String {
    return data?.message?.takeIf { it.isNotEmpty() }
        ?: data?.errors?.firstOrNull()
        ?: error?.message
        ?: default
}

// ── Request DTOs ─────────────────────────────────────────────────────────────

@Serializable
private data class RegisterRequestDto(
    val email: String,
    val password: String,
    @SerialName("confirm_password")
    val confirmPassword: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    @SerialName("accept_terms")
    val acceptTerms: Boolean
)

@Serializable
private data class VerifyEmailRequestDto(
    val email: String,
    @SerialName("verification_token")
    val verificationToken: String
)

@Serializable
private data class ResendVerificationRequestDto(
    val email: String
)

@Serializable
private data class ForgotPasswordRequestDto(
    val email: String
)

@Serializable
private data class ResetPasswordRequestDto(
    val token: String,
    @SerialName("new_password")
    val newPassword: String,
    @SerialName("confirm_password")
    val confirmPassword: String
)

// ── Response DTOs ────────────────────────────────────────────────────────────

@Serializable
internal data class RegisterResponseDto(
    val success: Boolean,
    @SerialName("user_id")
    val userId: String? = null,
    override val message: String = "",
    @SerialName("verification_required")
    val verificationRequired: Boolean = false,
    @SerialName("verification_email_sent")
    val verificationEmailSent: Boolean = false,
    val email: String? = null,
    val errors: List<String> = emptyList()
) : MessageResponse {
    fun toDomain(): RegisterResult = RegisterResult(
        userId = userId,
        email = email ?: "",
        verificationRequired = verificationRequired,
        verificationEmailSent = verificationEmailSent
    )
}

@Serializable
internal data class VerifyEmailResponseDto(
    val success: Boolean,
    override val message: String = "",
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("email_verified")
    val emailVerified: Boolean = false
) : MessageResponse {
    fun toDomain(): VerifyEmailResult = VerifyEmailResult(
        userId = userId,
        emailVerified = emailVerified
    )
}

@Serializable
internal data class ResendVerificationResponseDto(
    val success: Boolean,
    override val message: String = "",
    @SerialName("email_sent")
    val emailSent: Boolean = false,
    @SerialName("cooldown_minutes")
    val cooldownMinutes: Int? = null
) : MessageResponse

@Serializable
internal data class ForgotPasswordResponseDto(
    val success: Boolean,
    override val message: String = ""
) : MessageResponse

@Serializable
internal data class ResetPasswordResponseDto(
    val success: Boolean,
    override val message: String = "",
    @SerialName("password_changed")
    val passwordChanged: Boolean = false
) : MessageResponse

// ── Login Error Response DTO ────────────────────────────────────────────────

@Serializable
internal data class LoginErrorResponse(
    val success: Boolean,
    override val message: String = "",
    val error: LoginErrorDetail? = null
) : MessageResponse {
    @Serializable
    data class LoginErrorDetail(
        val category: String? = null,
        val code: Int? = null,
        val message: String? = null
    )
}

// ── Existing Response Models (Login/User) ────────────────────────────────────

@Serializable
private data class LoginResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: Long,
    @SerialName("token_type")
    val tokenType: String = "Bearer"
) {
    fun toAuthToken(): AuthToken = AuthToken.fromResponse(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn,
        tokenType = tokenType
    )
}

@Serializable
private data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    val role: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("email_verified")
    val emailVerified: Boolean = false
) {
    fun toDomain(): User {
        return User(
            id = id,
            username = username,
            email = email,
            fullName = fullName,
            avatarUrl = avatarUrl,
            role = when (role?.lowercase()) {
                "admin" -> UserRole.ADMIN
                "moderator" -> UserRole.MODERATOR
                "super_admin" -> UserRole.SUPER_ADMIN
                else -> UserRole.USER
            },
            isActive = isActive,
            emailVerified = emailVerified
        )
    }
}

// ── User Provider Response DTOs ─────────────────────────────────────────────

/**
 * Response DTO for checking if user has a provider.
 * Returns provider info if exists, null if user needs onboarding.
 */
@Serializable
internal data class UserProviderResponseDto(
    @SerialName("provider_id")
    val providerId: String? = null,
    @SerialName("provider_code")
    val providerCode: String? = null,
    @SerialName("business_name")
    val businessName: String? = null,
    @SerialName("has_outlet")
    val hasOutlet: Boolean = false,
    @SerialName("outlet_count")
    val outletCount: Int = 0,
    @SerialName("outlet_id")
    val outletId: String? = null
) {
    fun toDomain(): UserProviderResponse? {
        // If provider_id is null, user has no provider
        if (providerId == null) return null

        return UserProviderResponse(
            providerId = providerId,
            providerCode = providerCode ?: "",
            businessName = businessName ?: "",
            hasOutlet = hasOutlet,
            outletCount = outletCount,
            outletId = outletId
        )
    }
}

/**
 * Domain model for user provider response.
 * Contains the user's provider information if it exists.
 */
@Serializable
data class UserProviderResponse(
    @SerialName("provider_id")
    val providerId: String,
    @SerialName("provider_code")
    val providerCode: String,
    @SerialName("business_name")
    val businessName: String,
    @SerialName("has_outlet")
    val hasOutlet: Boolean,
    @SerialName("outlet_count")
    val outletCount: Int,
    @SerialName("outlet_id")
    val outletId: String? = null
)
