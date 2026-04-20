package id.startapp.pheromone.infrastructure.database.dao

import id.startapp.pheromone.domain.demo.entity.Todo
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Extension functions to convert between TodoEntity and Todo.
 */

/**
 * Convert TodoEntity to domain Todo.
 */
fun TodoEntity.toDomain(): Todo {
    return Todo(
        id = id,
        title = title,
        description = description,
        isCompleted = is_completed == 1L,
        priority = priority.toInt(),
        dueDate = due_date?.let { Instant.fromEpochMilliseconds(it) },
        createdAt = Instant.fromEpochMilliseconds(created_at),
        updatedAt = Instant.fromEpochMilliseconds(updated_at)
    )
}

/**
 * Convert domain Todo to TodoEntity.
 */
fun Todo.toEntity(): TodoEntity {
    return TodoEntity(
        id = id,
        title = title,
        description = description,
        is_completed = if (isCompleted) 1L else 0L,
        priority = priority.toLong(),
        due_date = dueDate?.toEpochMilliseconds(),
        created_at = createdAt.toEpochMilliseconds(),
        updated_at = Clock.System.now().toEpochMilliseconds(),
        synced_at = Clock.System.now().toEpochMilliseconds(),
        deleted_at = null
    )
}
