package id.startapp.pheromone.infrastructure.network

/**
 * Android implementation of BuildConfig.
 *
 * Values are read from Android's BuildConfig class generated at compile time.
 *
 * To customize these values, edit the androidApp/build.gradle.kts file:
 *
 * ```kotlin
 * android {
 *     defaultConfig {
 *         buildConfigField("String", "API_BASE_URL", "\"https://api.example.com\"")
 *         buildConfigField("boolean", "IS_DEBUG", "true")
 *         buildConfigField("Long", "REQUEST_TIMEOUT_MS", "30000L")
 *         buildConfigField("Long", "CONNECT_TIMEOUT_MS", "10000L")
 *         buildConfigField("Long", "SOCKET_TIMEOUT_MS", "30000L")
 *     }
 *     buildTypes {
 *         release {
 *             buildConfigField("String", "API_BASE_URL", "\"https://api.production.com\"")
 *             buildConfigField("boolean", "IS_DEBUG", "false")
 *         }
 *     }
 * }
 * ```
 */
actual object BuildConfig {
    actual val API_BASE_URL: String = try {
        Class.forName("id.startapp.pheromone.BuildConfig").let {
            it.getDeclaredField("API_BASE_URL").get(null) as? String
        } ?: getDefaultApiUrl()
    } catch (e: Exception) {
        getDefaultApiUrl()
    }

    actual val IS_DEBUG: Boolean = try {
        Class.forName("id.startapp.pheromone.BuildConfig").let {
            it.getDeclaredField("DEBUG").get(null) as? Boolean ?: true
        }
    } catch (e: Exception) {
        true
    }

    actual val VERSION_NAME: String = try {
        Class.forName("id.startapp.pheromone.BuildConfig").let {
            it.getDeclaredField("VERSION_NAME").get(null) as? String ?: "1.0.0"
        }
    } catch (e: Exception) {
        "1.0.0"
    }

    actual val VERSION_CODE: Int = try {
        Class.forName("id.startapp.pheromone.BuildConfig").let {
            it.getDeclaredField("VERSION_CODE").get(null) as? Int ?: 1
        }
    } catch (e: Exception) {
        1
    }

    // Default timeout values (can be overridden via BuildConfig)
    actual val REQUEST_TIMEOUT_MS: Long = try {
        Class.forName("id.startapp.pheromone.BuildConfig").let {
            (it.getDeclaredField("REQUEST_TIMEOUT_MS").get(null) as? Long)?.toLong() ?: 30000L
        }
    } catch (e: Exception) {
        30000L // 30 seconds
    }

    actual val CONNECT_TIMEOUT_MS: Long = try {
        Class.forName("id.startapp.pheromone.BuildConfig").let {
            (it.getDeclaredField("CONNECT_TIMEOUT_MS").get(null) as? Long)?.toLong() ?: 10000L
        }
    } catch (e: Exception) {
        10000L // 10 seconds
    }

    actual val SOCKET_TIMEOUT_MS: Long = try {
        Class.forName("id.startapp.pheromone.BuildConfig").let {
            (it.getDeclaredField("SOCKET_TIMEOUT_MS").get(null) as? Long)?.toLong() ?: 30000L
        }
    } catch (e: Exception) {
        30000L // 30 seconds
    }

    private fun getDefaultApiUrl(): String = "https://api.example.com"
}
