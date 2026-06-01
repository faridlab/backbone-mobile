package id.startapp.infrastructure.database

import app.cash.sqldelight.db.SqlDriver
import id.startapp.AppDatabase

/**
 * Database manager for SQLDelight.
 *
 * Provides a singleton instance of the SQLDelight database driver.
 * Platform-specific implementations are in androidMain/iosMain.
 */
object DatabaseManager {

    private var driver: SqlDriver? = null

    /**
     * Check if the database driver is initialized.
     */
    fun isInitialized(): Boolean = driver != null

    /**
     * Get the database driver, or null if not initialized.
     *
     * This is a null-safe alternative to getDriver() that won't throw.
     *
     * @return The driver, or null if not initialized
     */
    fun getDriverOrNull(): SqlDriver? = driver

    /**
     * Get or create the database driver.
     *
     * This function must be called from platform-specific code
     * to create the actual driver implementation.
     *
     * @throws IllegalStateException if driver not initialized
     */
    fun getDriver(): SqlDriver {
        return driver ?: throw IllegalStateException("Database driver not initialized. Call initDriver() first.")
    }

    /**
     * Initialize the database driver.
     *
     * @param driver Platform-specific SQL driver
     */
    fun initDriver(driver: SqlDriver) {
        if (this.driver != null) {
            return // Already initialized
        }
        this.driver = driver
    }

    /**
     * Close the database driver.
     */
    fun close() {
        driver?.close()
        driver = null
    }

    /**
     * Get the AppDatabase instance.
     * Returns null if the driver is not initialized.
     */
    fun getDatabaseOrNull(): AppDatabase? {
        return driver?.let { AppDatabase(it) }
    }

    /**
     * Get the AppDatabase instance.
     *
     * @throws IllegalStateException if driver not initialized
     */
    fun getDatabase(): AppDatabase {
        return AppDatabase(getDriver())
    }
}

/**
 * Expect function to create the platform-specific driver.
 * Actual implementations are in androidMain/iosMain.
 *
 * Note: On Android, use createDriverWithContext() with Koin's androidContext() instead.
 */
expect fun createDriver(): SqlDriver
