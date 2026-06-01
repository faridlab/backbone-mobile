package id.startapp.core.service

import id.startapp.core.logging.AppLogger
import id.startapp.core.logging.TaggedLogger
import id.startapp.core.mapper.BaseEntityMapper
import id.startapp.core.usecase.CrudUseCases
import id.startapp.core.validator.BaseEntityValidator
import id.startapp.domain.types.NetworkError
import id.startapp.domain.types.Result
import id.startapp.infrastructure.pagination.PaginatedApiResponse

// Wires together use cases, mapper, and validator into a single service layer
abstract class BaseCrudService<E, DTO, FormData>(
    protected val useCases: CrudUseCases<E>,
    protected val mapper: BaseEntityMapper<E, DTO, FormData>,
    protected val validator: BaseEntityValidator<FormData>,
) {
    // ── 8A — Structured logging ───────────────────────────────────────────────
    //
    // Override in a subclass to set a more specific tag, e.g.:
    //   override val logger = AppLogger.withTag("OrderService")
    protected open val logger: TaggedLogger =
        AppLogger.withTag(this::class.simpleName ?: "CrudService")

    suspend fun getById(id: String): Result<DTO> {
        logger.d { "getById($id)" }
        return useCases.getById(id).map { mapper.toDto(it) }
            .also { result ->
                when (result) {
                    is Result.Success -> logger.i { "getById($id): success" }
                    is Result.Error   -> logger.e(result.error) { "getById($id): failed" }
                }
            }
    }

    suspend fun getAll(
        page: Int = 1,
        limit: Int = 20,
        sortBy: String = "created_at",
        sortDesc: Boolean = true,
        filters: Map<String, String> = emptyMap(),
    ): Result<PaginatedApiResponse<DTO>> {
        logger.d { "getAll(page=$page, limit=$limit)" }
        return useCases.getAll(page, limit, sortBy, sortDesc, filters).map { paginated ->
            PaginatedApiResponse(
                data = paginated.data.map { mapper.toDto(it) },
                total = paginated.total,
                page = paginated.page,
                limit = paginated.limit,
                totalPages = paginated.totalPages,
                hasNext = paginated.hasNext,
                hasPrev = paginated.hasPrev,
            )
        }.also { result ->
            when (result) {
                is Result.Success -> logger.i { "getAll(page=$page): success — ${result.data.data.size} items" }
                is Result.Error   -> logger.e(result.error) { "getAll(page=$page): failed" }
            }
        }
    }

    suspend fun create(formData: FormData): Result<DTO> {
        logger.d { "create: validating" }
        val validation = validator.validate(formData)
        if (!validation.isValid) {
            val errors = validation.errors.mapValues { it.value.joinToString("; ") }
            logger.i { "create: validation failed — ${errors.keys.joinToString()}" }
            return Result.Error(NetworkError.ValidationError(errors))
        }
        return useCases.create(mapper.toEntity(formData)).map { mapper.toDto(it) }
            .also { result ->
                when (result) {
                    is Result.Success -> logger.i { "create: success" }
                    is Result.Error   -> logger.e(result.error) { "create: failed" }
                }
            }
    }

    suspend fun update(id: String, formData: FormData): Result<DTO> {
        logger.d { "update($id): validating" }
        val validation = validator.validate(formData)
        if (!validation.isValid) {
            val errors = validation.errors.mapValues { it.value.joinToString("; ") }
            logger.i { "update($id): validation failed — ${errors.keys.joinToString()}" }
            return Result.Error(NetworkError.ValidationError(errors))
        }
        val existing = useCases.getById(id)
        if (existing is Result.Error) return existing
        return useCases.update(id, mapper.toEntity(formData)).map { mapper.toDto(it) }
            .also { result ->
                when (result) {
                    is Result.Success -> logger.i { "update($id): success" }
                    is Result.Error   -> logger.e(result.error) { "update($id): failed" }
                }
            }
    }

    suspend fun delete(id: String): Result<Unit> =
        useCases.delete(id).also { result ->
            when (result) {
                is Result.Success -> logger.i { "delete($id): success" }
                is Result.Error   -> logger.e(result.error) { "delete($id): failed" }
            }
        }
}
