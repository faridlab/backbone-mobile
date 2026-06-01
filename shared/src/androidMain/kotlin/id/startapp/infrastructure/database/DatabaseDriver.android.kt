package id.startapp.infrastructure.database

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import id.startapp.AppDatabase

/**
 * Create the Android SQLDelight driver with the provided context.
 *
 * @param context Android application context
 * @return Android SQLite driver
 */
fun createDriverWithContext(context: android.content.Context): SqlDriver {
    return AndroidSqliteDriver(
        schema = AppDatabase.Schema,
        context = context,
        name = "backbone.db"
    )
}

/**
 * Actual implementation of createDriver for Android.
 * This should NOT be called directly - use createDriverWithContext from Koin module instead.
 *
 * @throws IllegalStateException if called (should use Koin-provided driver)
 */
actual fun createDriver(): SqlDriver {
    throw IllegalStateException(
        "Direct createDriver() call not supported on Android. " +
        "Use the SqlDriver provided by Koin DI instead."
    )
}

/**
 * Android implementation of transaction execution.
 * Uses SQLDelight's AppDatabase Transacter interface.
 */
actual fun <T> executeTransactionPlatform(
    driver: SqlDriver,
    block: () -> T
): T {
    val database = AppDatabase(driver)
    var result: T? = null
    database.transaction {
        result = block()
    }
    return result!!
}

/**
 * Android implementation of suspending transaction execution.
 * Uses SQLDelight's AppDatabase Transacter interface with coroutines.
 */
actual suspend fun <T> executeSuspendTransactionPlatform(
    driver: SqlDriver,
    block: suspend () -> T
): T {
    val database = AppDatabase(driver)
    var result: T? = null
    database.transaction {
        result = kotlinx.coroutines.runBlocking {
            block()
        }
    }
    return result!!
}
