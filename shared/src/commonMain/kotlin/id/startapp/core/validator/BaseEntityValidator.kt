package id.startapp.core.validator

data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, List<String>>,
) {
    companion object {
        val Valid = ValidationResult(isValid = true, errors = emptyMap())
        fun invalid(errors: Map<String, List<String>>) = ValidationResult(isValid = false, errors = errors)
    }
}

abstract class BaseEntityValidator<FormData> {
    abstract fun validate(formData: FormData): ValidationResult

    // Helper: drop empty-error entries so callers only see real violations
    protected fun buildErrors(vararg pairs: Pair<String, List<String>>): Map<String, List<String>> =
        pairs.filter { it.second.isNotEmpty() }.toMap()
}

// Single-responsibility rule applied to one field value
fun interface FieldRule<F> {
    fun validate(value: F): String?
}

// Apply a set of rules to a value and collect all error messages
fun <F> applyRules(value: F, vararg rules: FieldRule<F>): List<String> =
    rules.mapNotNull { it.validate(value) }

// ─── Built-in rules ───────────────────────────────────────────────────────────

fun requiredString(fieldName: String): FieldRule<String?> = FieldRule { value ->
    if (value.isNullOrBlank()) "$fieldName is required" else null
}

fun minLength(fieldName: String, min: Int): FieldRule<String?> = FieldRule { value ->
    if (!value.isNullOrBlank() && value.length < min) "$fieldName must be at least $min characters" else null
}

fun maxLength(fieldName: String, max: Int): FieldRule<String?> = FieldRule { value ->
    if (!value.isNullOrBlank() && value.length > max) "$fieldName must be at most $max characters" else null
}

fun <N : Comparable<N>> minValue(fieldName: String, min: N): FieldRule<N?> = FieldRule { value ->
    if (value != null && value < min) "$fieldName must be at least $min" else null
}

fun <N : Comparable<N>> maxValue(fieldName: String, max: N): FieldRule<N?> = FieldRule { value ->
    if (value != null && value > max) "$fieldName must be at most $max" else null
}

fun emailRule(fieldName: String): FieldRule<String?> = FieldRule { value ->
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    if (!value.isNullOrBlank() && !emailRegex.matches(value)) "$fieldName must be a valid email" else null
}
