package id.startapp.pheromone.infrastructure.repository

import id.startapp.pheromone.domain.demo.entity.Todo
import id.startapp.pheromone.domain.demo.repository.TodoRepository
import id.startapp.pheromone.domain.types.Result
import id.startapp.pheromone.domain.types.NetworkError
import id.startapp.pheromone.infrastructure.database.dao.TodoDao
import id.startapp.pheromone.infrastructure.database.dao.TodoEntity
import id.startapp.pheromone.infrastructure.database.dao.toDomain
import id.startapp.pheromone.infrastructure.database.dao.toEntity
import kotlinx.coroutines.CancellationException

/**
 * Implementation of TodoRepository.
 *
 * Provides local cache-first data loading with sync support.
 * For demo purposes, this is a local-only implementation.
 */
class TodoRepositoryImpl(
    private val todoDao: TodoDao
) : TodoRepository {

    override suspend fun getTodos(): Result<List<Todo>> {
        return try {
            val todos = todoDao.getAllTodosList().map { it.toDomain() }
            Result.Success(todos)
        } catch (e: CancellationException) {
            throw e  // Don't swallow cancellation
        } catch (e: Exception) {
            Result.Error(mapDatabaseException(e))
        }
    }

    override suspend fun getTodoById(id: String): Result<Todo> {
        return try {
            val todo = todoDao.getTodoById(id)?.toDomain()
                ?: return Result.Error(NetworkError.NotFound("Todo: $id"))
            Result.Success(todo)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(mapDatabaseException(e))
        }
    }

    override suspend fun getActiveTodos(): Result<List<Todo>> {
        return try {
            val todos = todoDao.getActiveTodosList()
                .map { it.toDomain() }
            Result.Success(todos)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(mapDatabaseException(e))
        }
    }

    override suspend fun getCompletedTodos(): Result<List<Todo>> {
        return try {
            val todos = todoDao.getCompletedTodosList()
                .map { it.toDomain() }
            Result.Success(todos)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(mapDatabaseException(e))
        }
    }

    override suspend fun createTodo(todo: Todo): Result<Todo> {
        return try {
            todoDao.insertTodo(todo.toEntity())
            Result.Success(todo)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(mapDatabaseException(e))
        }
    }

    override suspend fun updateTodo(todo: Todo): Result<Todo> {
        return try {
            todoDao.updateTodo(todo.toEntity())
            Result.Success(todo)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(mapDatabaseException(e))
        }
    }

    override suspend fun toggleTodoCompleted(id: String, isCompleted: Boolean): Result<Unit> {
        return try {
            todoDao.toggleTodoCompleted(id, isCompleted)
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(mapDatabaseException(e))
        }
    }

    override suspend fun deleteTodo(id: String): Result<Unit> {
        return try {
            todoDao.deleteTodo(id)
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(mapDatabaseException(e))
        }
    }

    /**
     * Synchronize local data with remote API.
     *
     * Note: Remote sync is a planned feature for future implementation.
     * This would fetch updates from the server and push local changes.
     * Current implementation is local-only.
     */
    override suspend fun sync(): Result<Unit> {
        // Remote sync is planned for future implementation.
        // Current implementation is local-only, so sync is a no-op.
        return Result.Success(Unit)
    }

    /**
     * Map exception to appropriate NetworkError.
     * Database exceptions are mapped to DatabaseError.
     */
    private fun mapDatabaseException(e: Exception): NetworkError {
        val message = e.message ?: ""
        val className = e::class.simpleName ?: ""
        return when {
            message.contains("database", ignoreCase = true) ||
            message.contains("SQL", ignoreCase = true) ||
            className.contains("Sql", ignoreCase = true) -> {
                NetworkError.DatabaseError(message)
            }
            else -> NetworkError.UnknownError(e)
        }
    }
}
