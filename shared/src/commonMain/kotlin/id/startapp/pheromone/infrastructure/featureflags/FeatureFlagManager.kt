package id.startapp.pheromone.infrastructure.featureflags
import kotlin.concurrent.Volatile

import id.startapp.pheromone.infrastructure.storage.KeyValueStorage
import kotlinx.coroutines.CancellationException

/**
 * Manages feature flag evaluation and local overrides.
 *
 * Resolution order (highest priority first):
 * 1. Local override (set via [setOverride])
 * 2. Remote config value (fetched via [RemoteConfigProvider])
 * 3. Flag default value ([FeatureFlag.defaultValue])
 *
 * @param storage KeyValueStorage for persisting local overrides
 * @param remoteConfigProvider Provider for remote flag values
 * @param isDebug When true, logs every flag evaluation to console
 */
class FeatureFlagManager(
    private val storage: KeyValueStorage,
    private val remoteConfigProvider: RemoteConfigProvider = NoOpRemoteConfigProvider(),
    private val isDebug: Boolean = false
) {

    @Volatile
    private var remoteValues: Map<String, Boolean> = emptyMap()

    /**
     * Check if a feature flag is enabled.
     *
     * @param flag The feature flag to evaluate
     * @return true if the flag is enabled, false otherwise
     */
    suspend fun isEnabled(flag: FeatureFlag): Boolean {
        // 1. Check local override
        val localOverride = storage.getBoolean(overrideKey(flag))
        if (localOverride != null) {
            if (isDebug) {
                println("FeatureFlagManager: ${flag.key} = $localOverride (local override)")
            }
            return localOverride
        }

        // 2. Check remote config
        val remoteValue = remoteValues[flag.key]
        if (remoteValue != null) {
            if (isDebug) {
                println("FeatureFlagManager: ${flag.key} = $remoteValue (remote config)")
            }
            return remoteValue
        }

        // 3. Fall back to default
        if (isDebug) {
            println("FeatureFlagManager: ${flag.key} = ${flag.defaultValue} (default)")
        }
        return flag.defaultValue
    }

    /**
     * Set a local override for a feature flag.
     * Local overrides take highest priority in flag evaluation.
     *
     * @param flag The feature flag to override
     * @param enabled The override value
     */
    suspend fun setOverride(flag: FeatureFlag, enabled: Boolean) {
        storage.putBoolean(overrideKey(flag), enabled)
        if (isDebug) {
            println("FeatureFlagManager: Override set ${flag.key} = $enabled")
        }
    }

    /**
     * Clear the local override for a feature flag.
     * The flag will revert to remote config or default value.
     *
     * @param flag The feature flag to clear
     */
    suspend fun clearOverride(flag: FeatureFlag) {
        storage.remove(overrideKey(flag))
        if (isDebug) {
            println("FeatureFlagManager: Override cleared for ${flag.key}")
        }
    }

    /**
     * Clear all local overrides.
     * All flags will revert to remote config or default values.
     */
    suspend fun clearAllOverrides() {
        FeatureFlag.entries.forEach { flag ->
            storage.remove(overrideKey(flag))
        }
        if (isDebug) {
            println("FeatureFlagManager: All overrides cleared")
        }
    }

    /**
     * Get all flags with their current evaluated values.
     *
     * @return Map of flag to its current resolved value
     */
    suspend fun getAllFlags(): Map<FeatureFlag, Boolean> {
        return FeatureFlag.entries.associateWith { isEnabled(it) }
    }

    /**
     * Refresh flag values from the remote config provider.
     *
     * Call this on app startup or periodically to pick up
     * remote flag changes. No-ops if remote config is not configured.
     */
    suspend fun refreshFromRemote() {
        try {
            if (!remoteConfigProvider.isInitialized) {
                remoteConfigProvider.init()
            }
            val fetched = remoteConfigProvider.fetchFlags()
            remoteValues = fetched
            if (isDebug) {
                if (fetched.isEmpty()) {
                    println("FeatureFlagManager: Remote config not configured or returned empty")
                } else {
                    println("FeatureFlagManager: Refreshed ${fetched.size} flags from remote")
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (isDebug) {
                println("FeatureFlagManager: Remote refresh failed: ${e.message}")
            }
        }
    }

    private fun overrideKey(flag: FeatureFlag): String =
        "feature_override_${flag.key}"
}
