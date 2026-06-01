package id.startapp.infrastructure.di

import id.startapp.domain.auth.entity.AuthToken
import id.startapp.infrastructure.database.DatabaseManager
import id.startapp.infrastructure.database.createDriver
import id.startapp.infrastructure.network.ConnectivityMonitor
import id.startapp.infrastructure.storage.KeyValueStorage
import id.startapp.infrastructure.storage.SecureStorage
import id.startapp.infrastructure.storage.TokenStorage
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

/**
 * iOS-specific DI module.
 *
 * Uses Swift KeychainHelper for secure storage via native interop.
 */
actual val platformModule: Module = module {
    // Platform-specific storage implementations
    single<SecureStorage> { IOSSecureStorage() }
    single<TokenStorage> { IOSTokenStorage(get()) }
    single<KeyValueStorage> { IOSKeyValueStorage() }

    // ConnectivityMonitor
    single { ConnectivityMonitor() }

    // Initialize database
    single {
        createDriver().also { driver ->
            DatabaseManager.initDriver(driver)
        }
    }
}

/**
 * iOS implementation of SecureStorage using Keychain.
 *
 * Uses Swift KeychainHelper for hardware-backed secure storage.
 * The Swift implementation must be added to the iOS Xcode project:
 * see ios/iosApp/KeychainHelper.swift
 */
class IOSSecureStorage : SecureStorage {

    /**
     * Store a value in Keychain.
     */
    private fun setKeychainValue(key: String, value: String): Boolean {
        // Calls Swift function via Kotlin/Native interop
        return KeychainHelper.set(key, value)
    }

    /**
     * Get a value from Keychain.
     */
    private fun getKeychainValue(key: String): String? {
        // Calls Swift function via Kotlin/Native interop
        return KeychainHelper.get(key)
    }

    /**
     * Delete a value from Keychain.
     */
    private fun deleteKeychainValue(key: String): Boolean {
        return KeychainHelper.delete(key)
    }

    override suspend fun storeAccessToken(token: String) {
        setKeychainValue(SecureStorage.KEY_ACCESS_TOKEN, token)
    }

    override suspend fun getAccessToken(): String? {
        return getKeychainValue(SecureStorage.KEY_ACCESS_TOKEN)
    }

    override suspend fun clearAccessToken() {
        deleteKeychainValue(SecureStorage.KEY_ACCESS_TOKEN)
    }

    override suspend fun storeRefreshToken(token: String) {
        setKeychainValue(SecureStorage.KEY_REFRESH_TOKEN, token)
    }

    override suspend fun getRefreshToken(): String? {
        return getKeychainValue(SecureStorage.KEY_REFRESH_TOKEN)
    }

    override suspend fun clearRefreshToken() {
        deleteKeychainValue(SecureStorage.KEY_REFRESH_TOKEN)
    }

    override suspend fun storeOAuthToken(provider: String, token: String) {
        setKeychainValue("${SecureStorage.KEY_OAUTH_PREFIX}$provider", token)
    }

    override suspend fun getOAuthToken(provider: String): String? {
        return getKeychainValue("${SecureStorage.KEY_OAUTH_PREFIX}$provider")
    }

    override suspend fun clearOAuthTokens() {
        // Clear all OAuth tokens with known prefixes
        val providers = listOf("google", "github", "facebook", "apple")
        providers.forEach { provider ->
            deleteKeychainValue("${SecureStorage.KEY_OAUTH_PREFIX}$provider")
        }
    }

    override suspend fun clearAll() {
        KeychainHelper.clearAll()
    }
}

/**
 * Swift KeychainHelper bridge functions.
 *
 * These functions must be implemented in Swift (see ios/iosApp/KeychainHelper.swift).
 * The Kotlin compiler will link to the native implementations at runtime.
 */
private object KeychainHelper {
    external fun set(key: String, value: String): Boolean
    external fun get(key: String): String?
    external fun delete(key: String): Boolean
    external fun clearAll(): Boolean
}

/**
 * Constants for token storage on iOS.
 */
private object IOSTokenStorageConstants {
    const val DEFAULT_TOKEN_EXPIRY_SECONDS = 3600L
}

/**
 * iOS implementation of TokenStorage.
 */
class IOSTokenStorage(
    private val secureStorage: SecureStorage
) : TokenStorage {

    override suspend fun storeToken(token: AuthToken) {
        secureStorage.storeAccessToken(token.accessToken)
        secureStorage.storeRefreshToken(token.refreshToken)
    }

    override suspend fun getToken(): AuthToken? {
        val accessToken = secureStorage.getAccessToken() ?: return null
        val refreshToken = secureStorage.getRefreshToken() ?: return null
        return AuthToken.fromResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = IOSTokenStorageConstants.DEFAULT_TOKEN_EXPIRY_SECONDS
        )
    }

    override suspend fun getAccessToken(): String? = secureStorage.getAccessToken()
    override suspend fun getRefreshToken(): String? = secureStorage.getRefreshToken()
    override suspend fun hasToken(): Boolean = getAccessToken() != null

    override suspend fun isTokenExpired(): Boolean = getToken()?.isExpired() ?: true
    override suspend fun isTokenExpiringSoon(withinSeconds: Long): Boolean = getToken()?.isExpiringSoon(withinSeconds) ?: true

    override suspend fun clearToken() {
        secureStorage.clearAccessToken()
        secureStorage.clearRefreshToken()
    }

    override suspend fun getTokenIssuedAt(): Long? = getToken()?.issuedAt
    override suspend fun getTokenExpiresAt(): Long? = getToken()?.expiresAt?.toEpochMilliseconds()?.div(1000)
}

/**
 * iOS implementation of KeyValueStorage using NSUserDefaults.
 *
 * This is for non-sensitive data like user preferences, settings, etc.
 * For sensitive data, use SecureStorage with Keychain.
 */
class IOSKeyValueStorage : KeyValueStorage {

    private val userDefaults = NSUserDefaults.standardUserDefaults

    override suspend fun putString(key: String, value: String?) {
        if (value != null) {
            userDefaults.setObject(value, key)
        } else {
            userDefaults.removeObjectForKey(key)
        }
    }

    override suspend fun getString(key: String): String? {
        return userDefaults.stringForKey(key)
    }

    override suspend fun putInt(key: String, value: Int?) {
        if (value != null) {
            userDefaults.setInteger(value.toLong(), key)
        } else {
            userDefaults.removeObjectForKey(key)
        }
    }

    override suspend fun getInt(key: String): Int? {
        if (userDefaults.objectForKey(key) == null) {
            return null
        }
        return userDefaults.integerForKey(key).toInt()
    }

    override suspend fun putBoolean(key: String, value: Boolean?) {
        if (value != null) {
            userDefaults.setBool(value, key)
        } else {
            userDefaults.removeObjectForKey(key)
        }
    }

    override suspend fun getBoolean(key: String): Boolean? {
        if (userDefaults.objectForKey(key) == null) {
            return null
        }
        return userDefaults.boolForKey(key)
    }

    override suspend fun putLong(key: String, value: Long?) {
        // NSUserDefaults doesn't have a Long type, store as NSNumber
        if (value != null) {
            userDefaults.setObject(value.toString(), key)
        } else {
            userDefaults.removeObjectForKey(key)
        }
    }

    override suspend fun getLong(key: String): Long? {
        val str = userDefaults.stringForKey(key)
        return str?.toLongOrNull()
    }

    override suspend fun remove(key: String) {
        userDefaults.removeObjectForKey(key)
    }

    override suspend fun clearAll() {
        // Clear known keys in userDefaults
        // Note: NSUserDefaults doesn't provide a simple way to clear all keys
        // In production, you'd track the keys you've set
        val knownKeys: List<String> = emptyList()
        knownKeys.forEach { key: String ->
            userDefaults.removeObjectForKey(key)
        }
    }
}

/**
 * Swift CertificateHelper bridge functions for certificate pinning.
 *
 * These functions must be implemented in Swift (see ios/iosApp/KeychainHelper.swift).
 */
private object CertificateHelper {
    external fun computeSha256HashFromByteArray(byteArray: ByteArray): String
    external fun computePublicKeyHashFromByteArray(byteArray: ByteArray): String
}

/**
 * Platform-specific SHA-256 hash computation for certificate pinning.
 * Calls Swift implementation via CertificateHelper.
 */
internal fun computeSha256HashIos(data: ByteArray): String {
    return CertificateHelper.computeSha256HashFromByteArray(data)
}

/**
 * Platform-specific public key hash computation for certificate pinning.
 * Calls Swift implementation via CertificateHelper.
 */
internal fun computePublicKeyHashIos(data: ByteArray): String {
    return CertificateHelper.computePublicKeyHashFromByteArray(data)
}
