package id.startapp.domain.auth.entity

/**
 * Domain model for registration result.
 *
 * Backend does not return tokens on registration —
 * user must verify their email first before they can log in.
 */
data class RegisterResult(
    val userId: String?,
    val email: String,
    val verificationRequired: Boolean,
    val verificationEmailSent: Boolean
)
