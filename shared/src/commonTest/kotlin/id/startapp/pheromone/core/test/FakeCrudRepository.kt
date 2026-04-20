package id.startapp.pheromone.core.test

import id.startapp.pheromone.core.usecase.CrudRepository
import id.startapp.pheromone.domain.types.NetworkError
import id.startapp.pheromone.domain.types.Result
import id.startapp.pheromone.infrastructure.pagination.PaginatedApiResponse

/**
 * In-memory fake implementation of [CrudRepository] for unit testing.
 *
 * Provides configurable success/failure behavior and call-count tracking so tests can
 * assert not only on *what* was returned, but on *how many times* each operation was invoked.
 *
 * ## Usage
 * ```kotlin
 * private val fakeRepo = FakeCrudRepository<Order>(idExtractor = { it.id })
 * private val useCases  = fakeRepo.toUseCases()
 * private val viewModel = OrderListViewModel(useCases)
 *
 * @Test fun `load fetches all items`() = runTest {
 *     fakeRepo.items += Order(id = "1", name = "Test Order")
 *     viewModel.onIntent(CrudListIntent.Load)
 *     assertEquals(1, viewModel.currentState.items.size)
 * }
 * ```
 *
 * @param E Entity type stored in this fake repository
 * @param items Mutable backing list (pre-populate for test setup)
 * @param idExtractor Lambda that extracts the string ID from an entity
 */
class FakeCrudRepository<E>(
    val items: MutableList<E> = mutableListOf(),
    private val idExtractor: (E) -> String,
) : CrudRepository<E> {

    // -------------------------------------------------------------------------
    // Call tracking
    // -------------------------------------------------------------------------

    var findByIdCallCount = 0
    var findAllCallCount = 0
    var createCallCount = 0
    var updateCallCount = 0
    var deleteCallCount = 0

    var lastFindByIdArg: String? = null
    var lastDeletedId: String? = null
    var lastCreatedEntity: E? = null
    var lastUpdatedId: String? = null
    var lastUpdatedEntity: E? = null

    // -------------------------------------------------------------------------
    // Configurable failure injection
    // -------------------------------------------------------------------------

    /** When true, every operation returns [errorOverride] instead of acting on [items]. */
    var shouldFail: Boolean = false

    /** The error returned when [shouldFail] is true. */
    var errorOverride: NetworkError = NetworkError.ServerError(statusCode = 500)

    // -------------------------------------------------------------------------
    // CrudRepository implementation
    // -------------------------------------------------------------------------

    override suspend fun findById(id: String): Result<E> {
        findByIdCallCount++
        lastFindByIdArg = id
        if (shouldFail) return Result.Error(errorOverride)
        val item = items.find { idExtractor(it) == id }
        return if (item != null) Result.Success(item)
        else Result.Error(NetworkError.NotFound(resource = id))
    }

    override suspend fun findAll(
        page: Int,
        limit: Int,
        sortBy: String,
        sortDesc: Boolean,
        filters: Map<String, String>,
    ): Result<PaginatedApiResponse<E>> {
        findAllCallCount++
        if (shouldFail) return Result.Error(errorOverride)
        val offset = (page - 1).coerceAtLeast(0) * limit.coerceAtLeast(1)
        val paged = items.drop(offset).take(limit.coerceAtLeast(0))
        val total = items.size
        val totalPages = if (limit <= 0) 0 else (total + limit - 1) / limit
        return Result.Success(
            PaginatedApiResponse(
                data = paged,
                total = total,
                page = page,
                limit = limit,
                totalPages = totalPages,
                hasNext = page < totalPages,
                hasPrev = page > 1,
            )
        )
    }

    override suspend fun create(entity: E): Result<E> {
        createCallCount++
        lastCreatedEntity = entity
        if (shouldFail) return Result.Error(errorOverride)
        items.add(entity)
        return Result.Success(entity)
    }

    override suspend fun update(id: String, entity: E): Result<E> {
        updateCallCount++
        lastUpdatedId = id
        lastUpdatedEntity = entity
        if (shouldFail) return Result.Error(errorOverride)
        val idx = items.indexOfFirst { idExtractor(it) == id }
        return if (idx >= 0) {
            items[idx] = entity
            Result.Success(entity)
        } else {
            Result.Error(NetworkError.NotFound(resource = id))
        }
    }

    override suspend fun delete(id: String): Result<Unit> {
        deleteCallCount++
        lastDeletedId = id
        if (shouldFail) return Result.Error(errorOverride)
        val removed = items.removeAll { idExtractor(it) == id }
        return if (removed) Result.Success(Unit)
        else Result.Error(NetworkError.NotFound(resource = id))
    }

    // -------------------------------------------------------------------------
    // Test helpers
    // -------------------------------------------------------------------------

    /**
     * Reset all items and call counters to their initial state.
     * Useful between tests that share a single repository instance.
     */
    fun reset() {
        items.clear()
        findByIdCallCount = 0
        findAllCallCount = 0
        createCallCount = 0
        updateCallCount = 0
        deleteCallCount = 0
        lastFindByIdArg = null
        lastDeletedId = null
        lastCreatedEntity = null
        lastUpdatedId = null
        lastUpdatedEntity = null
        shouldFail = false
        errorOverride = NetworkError.ServerError(statusCode = 500)
    }
}
