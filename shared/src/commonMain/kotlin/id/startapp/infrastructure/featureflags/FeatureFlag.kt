package id.startapp.infrastructure.featureflags

/**
 * Application feature flags.
 *
 * Each flag has a unique storage key, default value, and description.
 * Flags can be overridden locally via [FeatureFlagManager] or remotely
 * via a [RemoteConfigProvider] implementation.
 *
 * @property key Unique storage key for this flag
 * @property defaultValue Value used when no override is set
 * @property description Human-readable description of what this flag controls
 */
enum class FeatureFlag(
    val key: String,
    val defaultValue: Boolean,
    val description: String
) {
    PUSH_NOTIFICATIONS(
        key = "ff_push_notifications",
        defaultValue = false,
        description = "Enable push notification delivery and token registration"
    ),
    BACKGROUND_SYNC(
        key = "ff_background_sync",
        defaultValue = false,
        description = "Enable periodic background data synchronization"
    ),
    NEW_ORDER_FLOW(
        key = "ff_new_order_flow",
        defaultValue = false,
        description = "Enable redesigned order creation flow"
    ),
    DELIVERY_TRACKING(
        key = "ff_delivery_tracking",
        defaultValue = false,
        description = "Enable real-time delivery tracking on map"
    ),
    ANALYTICS_V2(
        key = "ff_analytics_v2",
        defaultValue = false,
        description = "Enable v2 analytics with enhanced event tracking"
    ),
    INVENTORY_ALERTS(
        key = "ff_inventory_alerts",
        defaultValue = true,
        description = "Enable low-stock inventory alerts for owners"
    ),
    PERFORMANCE_MONITORING(
        key = "ff_performance_monitoring",
        defaultValue = true,
        description = "Enable performance trace collection"
    ),
    OFFLINE_MODE(
        key = "ff_offline_mode",
        defaultValue = false,
        description = "Enable offline-first data access with local cache"
    );

    companion object {
        /** Storage key prefix for all feature flag overrides. */
        const val KEY_PREFIX = "ff_"

        /** Find a flag by its storage key, or null if not found. */
        fun fromKey(key: String): FeatureFlag? =
            entries.firstOrNull { it.key == key }
    }
}
