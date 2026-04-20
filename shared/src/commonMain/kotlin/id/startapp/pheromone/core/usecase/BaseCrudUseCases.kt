package id.startapp.pheromone.core.usecase

import id.startapp.pheromone.domain.types.Result
import id.startapp.pheromone.infrastructure.pagination.PaginatedApiResponse

// Generic repository contract — implemented by infrastructure adapters
interface CrudRepository<E> {
    suspend fun findById(id: String): Result<E>
    suspend fun findAll(
        page: Int,
        limit: Int,
        sortBy: String,
        sortDesc: Boolean,
        filters: Map<String, String>,
    ): Result<PaginatedApiResponse<E>>
    suspend fun create(entity: E): Result<E>
    suspend fun update(id: String, entity: E): Result<E>
    suspend fun delete(id: String): Result<Unit>
}

// Container that bundles all use cases for an entity type
data class CrudUseCases<E>(
    val getById: GetByIdUseCase<E>,
    val getAll: GetAllUseCase<E>,
    val create: CreateUseCase<E>,
    val update: UpdateUseCase<E>,
    val delete: DeleteUseCase<E>,
)

class GetByIdUseCase<E>(private val repository: CrudRepository<E>) {
    suspend operator fun invoke(id: String): Result<E> = repository.findById(id)
}

class GetAllUseCase<E>(private val repository: CrudRepository<E>) {
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 20,
        sortBy: String = "created_at",
        sortDesc: Boolean = true,
        filters: Map<String, String> = emptyMap(),
    ): Result<PaginatedApiResponse<E>> = repository.findAll(page, limit, sortBy, sortDesc, filters)
}

class CreateUseCase<E>(private val repository: CrudRepository<E>) {
    suspend operator fun invoke(entity: E): Result<E> = repository.create(entity)
}

class UpdateUseCase<E>(private val repository: CrudRepository<E>) {
    suspend operator fun invoke(id: String, entity: E): Result<E> = repository.update(id, entity)
}

class DeleteUseCase<E>(private val repository: CrudRepository<E>) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.delete(id)
}

fun <E> CrudRepository<E>.toUseCases(): CrudUseCases<E> = CrudUseCases(
    getById = GetByIdUseCase(this),
    getAll = GetAllUseCase(this),
    create = CreateUseCase(this),
    update = UpdateUseCase(this),
    delete = DeleteUseCase(this),
)
