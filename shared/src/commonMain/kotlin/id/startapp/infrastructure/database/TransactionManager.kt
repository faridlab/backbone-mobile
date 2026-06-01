package id.startapp.infrastructure.database

import app.cash.sqldelight.db.SqlDriver
import id.startapp.domain.types.NetworkError

/**
 * Transaction manager for database operations.
 *
 * Provides ACID transaction support using SQLDelight's transaction API.
 * All operations in a transaction block are atomic, consistent, isolated, and durable.
 *
 * Note: SQLDelight transactions are platform-specific.
 * This implementation uses the AppDatabase Transacter interface.
 */
object TransactionManager {

    /**
     * Execute a block of code within a database transaction.
     *
     * The transaction is committed if the block completes successfully.
     * If an exception is thrown, the transaction is rolled back.
     *
     * @param block The transaction block to execute
     * @return The result of the transaction
     * @throws IllegalStateException if database driver is not initialized
     * @throws Exception if the transaction block fails
     */
    fun <T> transaction(block: () -> T): T {
        val driver = DatabaseManager.getDriverOrNull()
            ?: throw IllegalStateException("Database not initialized. Ensure Koin DI is properly set up.")

        return executeTransactionPlatform(driver, block)
    }

    /**
     * Execute a suspending block of code within a database transaction.
     *
     * The transaction is committed if the block completes successfully.
     * If an exception is thrown, the transaction is rolled back.
     *
     * @param block The suspending transaction block to execute
     * @return The result of the transaction
     * @throws IllegalStateException if database driver is not initialized
     * @throws Exception if the transaction block fails
     */
    suspend fun <T> suspendTransaction(block: suspend () -> T): T {
        val driver = DatabaseManager.getDriverOrNull()
            ?: throw IllegalStateException("Database not initialized. Ensure Koin DI is properly set up.")

        return executeSuspendTransactionPlatform(driver, block)
    }

    /**
     * Execute a block of code within a transaction with exception handling.
     *
     * Returns Result.Success on success, Result.Error on failure.
     * The transaction is automatically rolled back on failure.
     *
     * @param block The transaction block to execute
     * @return Result containing T on success, NetworkError on failure
     */
    fun <T> transactionResult(
        block: () -> T
    ): id.startapp.domain.types.Result<T> {
        return try {
            val result = transaction(block)
            id.startapp.domain.types.Result.Success(result)
        } catch (e: Exception) {
            id.startapp.domain.types.Result.Error(mapException(e))
        }
    }

    /**
     * Execute a suspending block of code within a transaction with exception handling.
     *
     * Returns Result.Success on success, Result.Error on failure.
     * The transaction is automatically rolled back on failure.
     *
     * @param block The suspending transaction block to execute
     * @return Result containing T on success, NetworkError on failure
     */
    suspend fun <T> suspendTransactionResult(
        block: suspend () -> T
    ): id.startapp.domain.types.Result<T> {
        return try {
            val result = suspendTransaction(block)
            id.startapp.domain.types.Result.Success(result)
        } catch (e: Exception) {
            id.startapp.domain.types.Result.Error(mapException(e))
        }
    }

    /**
     * Execute multiple operations in a single transaction.
     *
     * Use this when you need to execute multiple database operations atomically.
     * If any operation fails, all operations are rolled back.
     *
     * @param operations List of operations to execute
     * @return Result containing list of results on success
     */
    suspend fun <T> executeInTransaction(
        vararg operations: suspend () -> T
    ): id.startapp.domain.types.Result<List<T>> {
        return suspendTransactionResult {
            operations.map { it() }
        }
    }

    /**
     * Map exception to appropriate NetworkError.
     */
    private fun mapException(e: Exception): NetworkError {
        val message = e.message ?: ""
        val className = e::class.simpleName ?: ""
        return when {
            message.contains("database", ignoreCase = true) ||
                message.contains("SQL", ignoreCase = true) ||
                className.contains("Sql", ignoreCase = true) -> {
                NetworkError.DatabaseError(message)
            }
            message.contains("constraint", ignoreCase = true) ||
                message.contains("unique", ignoreCase = true) -> {
                NetworkError.ValidationError(mapOf("general" to "A record with this value already exists"))
            }
            else -> NetworkError.UnknownError(e)
        }
    }
}

/**
 * Extension function to execute operations on a list within a transaction.
 *
 * @param operations List of operations to execute
 * @return Result containing list of results on success
 */
suspend fun <T> List<suspend () -> T>.executeInTransaction(
): id.startapp.domain.types.Result<List<T>> {
    return TransactionManager.executeInTransaction(*this.toTypedArray())
}

/**
 * Platform-specific transaction execution.
 * Implemented in androidMain and iosMain.
 */
expect fun <T> executeTransactionPlatform(
    driver: SqlDriver,
    block: () -> T
): T

/**
 * Platform-specific suspending transaction execution.
 * Implemented in androidMain and iosMain.
 */
expect suspend fun <T> executeSuspendTransactionPlatform(
    driver: SqlDriver,
    block: suspend () -> T
): T
