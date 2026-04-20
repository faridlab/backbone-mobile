package id.startapp.pheromone.infrastructure.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import id.startapp.pheromone.AppDatabase
import id.startapp.pheromone.domain.auth.entity.AuthToken
import id.startapp.pheromone.infrastructure.database.DatabaseManager
import id.startapp.pheromone.infrastructure.database.createDriverWithContext
import id.startapp.pheromone.infrastructure.network.ConnectivityMonitor
import id.startapp.pheromone.infrastructure.storage.KeyValueStorage
import id.startapp.pheromone.infrastructure.storage.SecureStorage
import id.startapp.pheromone.infrastructure.storage.TokenStorage
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific DI module.
 */
actual val platformModule = module {
    // Platform-specific storage implementations
    single<SecureStorage> { AndroidSecureStorage(androidContext()) }
    single<TokenStorage> { AndroidTokenStorage(get()) }
    single<KeyValueStorage> { AndroidKeyValueStorage(androidContext()) }

    // ConnectivityMonitor - requires Android context
    single { ConnectivityMonitor(androidContext()) }

    // SqlDriver - created with context and initialized in DatabaseManager
    single<app.cash.sqldelight.db.SqlDriver> {
        createDriverWithContext(androidContext()).also { driver ->
            DatabaseManager.initDriver(driver)
        }
    }
}

/**
 * Android implementation of SecureStorage using EncryptedSharedPreferences.
 *
 * Uses AndroidX Security library for hardware-backed encryption.
 * Keys are stored in Android Keystore, data is encrypted with AES256-GCM.
 */
class AndroidSecureStorage(
    private val context: Context
) : SecureStorage {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(false)
            .build()
    }

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun storeAccessToken(token: String) {
        prefs.edit().putString(SecureStorage.KEY_ACCESS_TOKEN, token).apply()
    }

    override suspend fun getAccessToken(): String? {
        return prefs.getString(SecureStorage.KEY_ACCESS_TOKEN, null)
    }

    override suspend fun clearAccessToken() {
        prefs.edit().remove(SecureStorage.KEY_ACCESS_TOKEN).apply()
    }

    override suspend fun storeRefreshToken(token: String) {
        prefs.edit().putString(SecureStorage.KEY_REFRESH_TOKEN, token).apply()
    }

    override suspend fun getRefreshToken(): String? {
        return prefs.getString(SecureStorage.KEY_REFRESH_TOKEN, null)
    }

    override suspend fun clearRefreshToken() {
        prefs.edit().remove(SecureStorage.KEY_REFRESH_TOKEN).apply()
    }

    override suspend fun storeOAuthToken(provider: String, token: String) {
        prefs.edit().putString("${SecureStorage.KEY_OAUTH_PREFIX}$provider", token).apply()
    }

    override suspend fun getOAuthToken(provider: String): String? {
        return prefs.getString("${SecureStorage.KEY_OAUTH_PREFIX}$provider", null)
    }

    override suspend fun clearOAuthTokens() {
        val editor = prefs.edit()
        prefs.all.keys.filter { it.startsWith(SecureStorage.KEY_OAUTH_PREFIX) }
            .forEach { editor.remove(it) }
        editor.apply()
    }

    override suspend fun clearAll() {
        prefs.edit().clear().apply()
    }
}

/**
 * Constants for token storage.
 */
private object TokenStorageConstants {
    const val DEFAULT_TOKEN_EXPIRY_SECONDS = 3600L
}

/**
 * Android implementation of TokenStorage.
 */
class AndroidTokenStorage(
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
            expiresIn = TokenStorageConstants.DEFAULT_TOKEN_EXPIRY_SECONDS
        )
    }

    override suspend fun getAccessToken(): String? {
        return secureStorage.getAccessToken()
    }

    override suspend fun getRefreshToken(): String? {
        return secureStorage.getRefreshToken()
    }

    override suspend fun hasToken(): Boolean {
        return getAccessToken() != null
    }

    override suspend fun isTokenExpired(): Boolean {
        return getToken()?.isExpired() ?: true
    }

    override suspend fun isTokenExpiringSoon(withinSeconds: Long): Boolean {
        return getToken()?.isExpiringSoon(withinSeconds) ?: true
    }

    override suspend fun clearToken() {
        secureStorage.clearAccessToken()
        secureStorage.clearRefreshToken()
    }

    override suspend fun getTokenIssuedAt(): Long? {
        return getToken()?.issuedAt
    }

    override suspend fun getTokenExpiresAt(): Long? {
        return getToken()?.expiresAt?.toEpochMilliseconds()?.div(1000)
    }
}

/**
 * Android implementation of KeyValueStorage using SharedPreferences.
 *
 * This is for non-sensitive data like user preferences, settings, etc.
 * For sensitive data, use SecureStorage with EncryptedSharedPreferences.
 */
class AndroidKeyValueStorage(
    private val context: Context
) : KeyValueStorage {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    override suspend fun putString(key: String, value: String?) {
        val editor = prefs.edit()
        if (value != null) {
            editor.putString(key, value)
        } else {
            editor.remove(key)
        }
        editor.apply()
    }

    override suspend fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    override suspend fun putInt(key: String, value: Int?) {
        val editor = prefs.edit()
        if (value != null) {
            editor.putInt(key, value)
        } else {
            editor.remove(key)
        }
        editor.apply()
    }

    override suspend fun getInt(key: String): Int? {
        if (!prefs.contains(key)) return null
        return prefs.getInt(key, 0)
    }

    override suspend fun putBoolean(key: String, value: Boolean?) {
        val editor = prefs.edit()
        if (value != null) {
            editor.putBoolean(key, value)
        } else {
            editor.remove(key)
        }
        editor.apply()
    }

    override suspend fun getBoolean(key: String): Boolean? {
        if (!prefs.contains(key)) return null
        return prefs.getBoolean(key, false)
    }

    override suspend fun putLong(key: String, value: Long?) {
        val editor = prefs.edit()
        if (value != null) {
            editor.putLong(key, value)
        } else {
            editor.remove(key)
        }
        editor.apply()
    }

    override suspend fun getLong(key: String): Long? {
        if (!prefs.contains(key)) return null
        return prefs.getLong(key, 0L)
    }

    override suspend fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    override suspend fun clearAll() {
        prefs.edit().clear().apply()
    }
}
