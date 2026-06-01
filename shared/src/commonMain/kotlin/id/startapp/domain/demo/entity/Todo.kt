package id.startapp.domain.demo.entity

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Todo entity for demo CRUD operations.
 *
 * Demonstrates the Backbone app patterns with:
 * - Clean architecture layers
 * - SQLDelight local storage
 * - Offline-first sync capabilities
 *
 * @property id Unique identifier
 * @property title Todo title
 * @property description Optional description
 * @property isCompleted Whether the todo is completed
 * @property priority Priority level (0 = low, 1 = medium, 2 = high)
 * @property dueDate Optional due date
 * @property createdAt Creation timestamp
 * @property updatedAt Last update timestamp
 */
@Serializable
data class Todo(
    val id: String,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val priority: Int = 0,
    val dueDate: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant = createdAt
) {

    /**
     * Check if the todo is overdue.
     */
    val isOverdue: Boolean
        get() = dueDate?.let {
            !isCompleted && Clock.System.now() > it
        } ?: false

    /**
     * Get the priority label.
     */
    val priorityLabel: String
        get() = when (priority) {
            2 -> "High"
            1 -> "Medium"
            else -> "Low"
        }

    companion object {

        /**
         * Create a new todo with generated ID.
         */
        fun create(
            title: String,
            description: String? = null,
            priority: Int = 0,
            dueDate: Instant? = null
        ): Todo {
            val now = Clock.System.now()
            return Todo(
                id = generateId(),
                title = title,
                description = description,
                isCompleted = false,
                priority = priority,
                dueDate = dueDate,
                createdAt = now,
                updatedAt = now
            )
        }

        private fun generateId(): String {
            return "todo_${Clock.System.now().toEpochMilliseconds()}_${(0..999).random()}"
        }
    }
}

/**
 * Priority enum for Todos.
 */
enum class TodoPriority(val value: Int) {
    LOW(0),
    MEDIUM(1),
    HIGH(2);

    companion object {
        fun fromInt(value: Int): TodoPriority {
            return entries.firstOrNull { it.value == value } ?: LOW
        }
    }
}
