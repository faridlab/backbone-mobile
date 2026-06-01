package id.startapp.infrastructure.database.dao

import id.startapp.AppDatabase
import id.startapp.infrastructure.database.DatabaseManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

/**
 * Data Access Object for Todo operations.
 *
 * Provides coroutine-aware methods for Todo CRUD operations.
 */
class TodoDao {

    private val queries: id.startapp.AppDatabase
        get() = DatabaseManager.getDatabase()

    /**
     * Get all todos as a Flow.
     */
    fun getAllTodos(): Flow<List<TodoEntity>> = flow {
        emit(getAllTodosList())
    }

    /**
     * Get all todos (suspend function).
     */
    suspend fun getAllTodosList(): List<TodoEntity> {
        return queries.todoQueriesQueries.selectAllTodos()
            .executeAsList()
            .map { it.toEntity() }
    }

    /**
     * Get a todo by ID.
     */
    suspend fun getTodoById(id: String): TodoEntity? {
        return queries.todoQueriesQueries.selectTodoById(id)
            .executeAsOneOrNull()
            ?.toEntity()
    }

    /**
     * Get completed todos using SQL WHERE clause.
     */
    fun getCompletedTodos(): Flow<List<TodoEntity>> = flow {
        emit(getCompletedTodosList())
    }

    /**
     * Get completed todos as a list.
     */
    suspend fun getCompletedTodosList(): List<TodoEntity> {
        return queries.todoQueriesQueries.selectCompletedTodos()
            .executeAsList()
            .map { it.toEntity() }
    }

    /**
     * Get active (not completed) todos using SQL WHERE clause.
     */
    fun getActiveTodos(): Flow<List<TodoEntity>> = flow {
        emit(getActiveTodosList())
    }

    /**
     * Get active todos as a list.
     */
    suspend fun getActiveTodosList(): List<TodoEntity> {
        return queries.todoQueriesQueries.selectActiveTodos()
            .executeAsList()
            .map { it.toEntity() }
    }

    /**
     * Get count of active todos.
     */
    suspend fun countActiveTodos(): Long {
        return queries.todoQueriesQueries.countActiveTodos()
            .executeAsOne()
    }

    /**
     * Insert a new todo.
     */
    suspend fun insertTodo(todo: TodoEntity) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.todoQueriesQueries.insertTodo(
            id = todo.id,
            title = todo.title,
            description = todo.description,
            is_completed = todo.is_completed,
            priority = todo.priority,
            due_date = todo.due_date,
            created_at = todo.created_at,
            updated_at = now,
            synced_at = now
        )
    }

    /**
     * Update an existing todo.
     */
    suspend fun updateTodo(todo: TodoEntity) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.todoQueriesQueries.updateTodo(
            title = todo.title,
            description = todo.description,
            is_completed = todo.is_completed,
            priority = todo.priority,
            due_date = todo.due_date,
            updated_at = now,
            synced_at = now,
            id = todo.id
        )
    }

    /**
     * Toggle todo completion status.
     */
    suspend fun toggleTodoCompleted(id: String, isCompleted: Boolean) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.todoQueriesQueries.toggleTodoCompleted(
            is_completed = if (isCompleted) 1L else 0L,
            updated_at = now,
            synced_at = now,
            id = id
        )
    }

    /**
     * Soft delete a todo.
     */
    suspend fun deleteTodo(id: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.todoQueriesQueries.deleteTodo(
            deleted_at = now,
            updated_at = now,
            synced_at = now,
            id = id
        )
    }

    /**
     * Permanently delete a todo.
     */
    suspend fun hardDeleteTodo(id: String) {
        queries.todoQueriesQueries.hardDeleteTodo(id)
    }

    /**
     * Get todos pending sync.
     */
    suspend fun getPendingSyncTodos(beforeTimestamp: Long): List<TodoEntity> {
        return queries.todoQueriesQueries.selectPendingSyncTodos(beforeTimestamp)
            .executeAsList()
            .map { it.toEntity() }
    }

    /**
     * Delete all todos.
     */
    suspend fun deleteAllTodos() {
        queries.todoQueriesQueries.deleteAllTodos()
    }
}

/**
 * Todo entity matching the SQLDelight generated schema.
 */
data class TodoEntity(
    val id: String,
    val title: String,
    val description: String?,
    val is_completed: Long,
    val priority: Long,
    val due_date: Long?,
    val created_at: Long,
    val updated_at: Long,
    val synced_at: Long,
    val deleted_at: Long?
)

/**
 * Convert generated Todo to TodoEntity.
 */
private fun id.startapp.Todo.toEntity(): TodoEntity {
    return TodoEntity(
        id = id,
        title = title,
        description = description,
        is_completed = is_completed,
        priority = priority,
        due_date = due_date,
        created_at = created_at,
        updated_at = updated_at,
        synced_at = synced_at,
        deleted_at = deleted_at
    )
}
