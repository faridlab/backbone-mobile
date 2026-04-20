package id.startapp.pheromone.infrastructure.cache

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Per-entity TTL configuration for the offline cache.
 *
 * TTLs are tuned to match how frequently each entity type changes:
 * - Active orders: short TTL (status changes frequently during processing)
 * - Reference data: long TTL (rarely changes, admin-managed)
 */
object CacheTTL {
    // Tier 1 - Critical (Business Operations)
    val ACTIVE_ORDER: Duration = 2.hours
    val COMPLETED_ORDER: Duration = 24.hours
    val ORDER_ITEMS: Duration = 2.hours
    val ORDER_STATUS_TIMELINE: Duration = 2.hours
    val ORDER_PICKUP: Duration = 2.hours
    val ORDER_DELIVERY: Duration = 2.hours
    val PAYMENT: Duration = 4.hours
    val CUSTOMER: Duration = 12.hours

    // Tier 2 - Important (Service Configuration)
    val PROVIDER_SERVICES: Duration = 7.days
    val SERVICE_CATEGORIES: Duration = 7.days
    val SERVICE_TYPES: Duration = 7.days
    val DELIVERY_TIERS: Duration = 7.days
    val AGENT: Duration = 12.hours
    val AGENT_TASK: Duration = 2.hours
    val PROVIDER_OUTLET: Duration = 24.hours
    val PROVIDER_STAFF: Duration = 12.hours

    // Tier 3 - Supporting (Reference Data)
    val ADDRESS: Duration = 12.hours
    val GEO_DATA: Duration = 30.days
    val PROVIDER_EQUIPMENT: Duration = 24.hours

    // Tier 4 - Reference (Content & Settings)
    val BANNERS: Duration = 24.hours
    val NOTIFICATIONS: Duration = 4.hours
    val SETTINGS: Duration = 30.days
    val ORDER_REVIEWS: Duration = 12.hours

    // Warehouse & Stock Management
    val WAREHOUSE: Duration = 24.hours
    val STOCK: Duration = 2.hours
    val GOODS_RECEIPT: Duration = 4.hours
    val STOCK_TRANSFER: Duration = 4.hours
    val STOCK_ADJUSTMENT: Duration = 4.hours

    // Financial
    val INVOICE: Duration = 4.hours
    val SETTLEMENT: Duration = 4.hours
    val ACCOUNT: Duration = 30.days

    // Promo & Inventory
    val PROMO: Duration = 7.days
    val INVENTORY: Duration = 12.hours

    // Default for unlisted entities
    val DEFAULT: Duration = 30.minutes
}

/** Convert a [Duration] to milliseconds for [CacheDao.put]. */
val Duration.inWholeMillisecondsLong: Long get() = inWholeMilliseconds
