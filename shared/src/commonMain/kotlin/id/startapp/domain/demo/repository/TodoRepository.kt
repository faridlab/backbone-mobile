package id.startapp.domain.demo.repository

import id.startapp.domain.demo.entity.Todo
import id.startapp.domain.types.Result

/**
 * Repository interface for Todo CRUD operations.
 *
 * Demonstrates the repository pattern with:
 * - Local cache-first data loading
 * - Background sync when online
 * - Offline support
 */
interface TodoRepository {

    /**
     * Get all todos.
     *
     * Returns cached data immediately and syncs in background.
     */
    suspend fun getTodos(): Result<List<Todo>>

    /**
     * Get a todo by ID.
     */
    suspend fun getTodoById(id: String): Result<Todo>

    /**
     * Get active (not completed) todos.
     */
    suspend fun getActiveTodos(): Result<List<Todo>>

    /**
     * Get completed todos.
     */
    suspend fun getCompletedTodos(): Result<List<Todo>>

    /**
     * Create a new todo.
     */
    suspend fun createTodo(todo: Todo): Result<Todo>

    /**
     * Update an existing todo.
     */
    suspend fun updateTodo(todo: Todo): Result<Todo>

    /**
     * Toggle todo completion status.
     */
    suspend fun toggleTodoCompleted(id: String, isCompleted: Boolean): Result<Unit>

    /**
     * Delete a todo (soft delete).
     */
    suspend fun deleteTodo(id: String): Result<Unit>

    /**
     * Sync pending changes with server.
     */
    suspend fun sync(): Result<Unit>
}
