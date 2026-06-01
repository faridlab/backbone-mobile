package id.startapp.infrastructure.featureflags

/**
 * Provider for remote feature flag configuration.
 *
 * Implementations fetch flag values from a remote service
 * (e.g., Firebase Remote Config, LaunchDarkly, custom backend).
 *
 * To activate remote config:
 * 1. Implement this interface with your chosen service SDK
 * 2. Replace [NoOpRemoteConfigProvider] in DiModule.kt
 */
interface RemoteConfigProvider {

    /** Whether the provider has been initialized and fetched at least once. */
    val isInitialized: Boolean

    /** Initialize the remote config provider. */
    suspend fun init()

    /**
     * Fetch the latest flag values from the remote service.
     *
     * @return Map of flag keys to their boolean values.
     *         Only flags present in the map will override defaults.
     */
    suspend fun fetchFlags(): Map<String, Boolean>
}

/**
 * Default no-op implementation that returns no remote overrides.
 *
 * Replace this with a real implementation when integrating
 * Firebase Remote Config or a custom feature flag backend.
 */
class NoOpRemoteConfigProvider : RemoteConfigProvider {

    override val isInitialized: Boolean = true

    override suspend fun init() {
        // No-op: remote config not configured
    }

    override suspend fun fetchFlags(): Map<String, Boolean> = emptyMap()
}
