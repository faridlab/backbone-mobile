package id.startapp.infrastructure.storage

/**
 * Simple key-value storage interface for non-sensitive data.
 *
 * Platform implementations:
 * - Android: SharedPreferences
 * - iOS: NSUserDefaults
 *
 * Use for:
 * - User preferences
 * - Settings
 * - Caching non-sensitive data
 *
 * Do NOT use for:
 * - Auth tokens (use SecureStorage instead)
 * - Personal data (use SecureStorage instead)
 */
interface KeyValueStorage {

    /**
     * Store a string value.
     */
    suspend fun putString(key: String, value: String?)

    /**
     * Get a string value.
     */
    suspend fun getString(key: String): String?

    /**
     * Store an integer value.
     */
    suspend fun putInt(key: String, value: Int?)

    /**
     * Get an integer value.
     */
    suspend fun getInt(key: String): Int?

    /**
     * Store a boolean value.
     */
    suspend fun putBoolean(key: String, value: Boolean?)

    /**
     * Get a boolean value.
     */
    suspend fun getBoolean(key: String): Boolean?

    /**
     * Store a long value.
     */
    suspend fun putLong(key: String, value: Long?)

    /**
     * Get a long value.
     */
    suspend fun getLong(key: String): Long?

    /**
     * Remove a specific key.
     */
    suspend fun remove(key: String)

    /**
     * Clear all stored data.
     */
    suspend fun clearAll()
}
