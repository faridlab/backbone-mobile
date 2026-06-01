package id.startapp.infrastructure.database

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import id.startapp.AppDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSHomeDirectory

/**
 * Actual implementation of createDriver for iOS.
 *
 * This matches the expect function in DatabaseManager.kt.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun createDriver(): SqlDriver {
    val documentsDirectory = NSHomeDirectory()
    val databasePath = "$documentsDirectory/backbone.db"

    return NativeSqliteDriver(
        schema = AppDatabase.Schema,
        name = databasePath
    )
}

/**
 * iOS implementation of transaction execution.
 * Uses SQLDelight's AppDatabase Transacter interface.
 */
actual fun <T> executeTransactionPlatform(
    driver: SqlDriver,
    block: () -> T
): T {
    // Create an AppDatabase instance to access the Transacter interface
    val database = AppDatabase(driver)
    // The transaction method expects a block that receives Transacter as receiver
    // We need to invoke our block within that context
    var result: T? = null
    database.transaction {
        result = block()
    }
    return result!!
}

/**
 * iOS implementation of suspending transaction execution.
 * Uses SQLDelight's AppDatabase Transacter interface with coroutines.
 */
actual suspend fun <T> executeSuspendTransactionPlatform(
    driver: SqlDriver,
    block: suspend () -> T
): T {
    // Create an AppDatabase instance to access the Transacter interface
    val database = AppDatabase(driver)
    // For suspend transactions, we need to run blocking within the synchronous transaction
    var result: T? = null
    database.transaction {
        result = kotlinx.coroutines.runBlocking {
            block()
        }
    }
    return result!!
}
