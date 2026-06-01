package id.startapp.core.viewmodel

import androidx.compose.runtime.Stable
import id.startapp.core.usecase.CrudUseCases
import id.startapp.domain.types.NetworkError
import id.startapp.domain.types.Result
import id.startapp.infrastructure.monitoring.CrashReporter
import id.startapp.presentation.viewmodel.BaseViewModel
import id.startapp.presentation.viewmodel.UiEffect
import id.startapp.presentation.viewmodel.UiIntent
import id.startapp.presentation.viewmodel.UiState

// ─── State / Intent / Effect ──────────────────────────────────────────────────

// 7A — @Stable tells Compose that CrudListState changes are always notified through
// the StateFlow, so it can skip recomposition when the reference is unchanged.
@Stable
data class CrudListState<DTO>(
    val items: List<DTO> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: NetworkError? = null,
    val page: Int = 1,
    val hasNext: Boolean = false,
    val selectedId: String? = null,
) : UiState

sealed class CrudListIntent : UiIntent {
    object Load : CrudListIntent()
    object Refresh : CrudListIntent()
    object LoadNextPage : CrudListIntent()
    data class Select(val id: String) : CrudListIntent()
    data class Delete(val id: String) : CrudListIntent()
}

sealed class CrudListEffect : UiEffect {
    data class ShowError(val error: NetworkError) : CrudListEffect()
    data class NavigateToDetail(val id: String) : CrudListEffect()
    object DeleteSuccess : CrudListEffect()
}

// ─── Base ViewModel ───────────────────────────────────────────────────────────

abstract class BaseCrudListViewModel<E, DTO>(
    private val useCases: CrudUseCases<E>,
) : BaseViewModel<CrudListState<DTO>, CrudListIntent, CrudListEffect>() {

    override fun initialState(): CrudListState<DTO> = CrudListState()

    // Subclass converts domain entity to display DTO
    protected abstract fun mapToDto(entity: E): DTO

    // Subclass provides entity ID extraction for optimistic list removal
    protected abstract fun extractId(dto: DTO): String

    // ── 6D — Analytics / observability hooks ──────────────────────────────────
    //
    // Override any of these in a generated or custom ViewModel subclass to
    // instrument load lifecycle events (e.g. Firebase Analytics, Mixpanel).
    //
    // Example:
    //   override fun onLoadStarted() = analytics.logEvent("order_list_load_started")
    //   override fun onLoadCompleted(itemCount: Int) =
    //       analytics.logEvent("order_list_loaded", mapOf("count" to itemCount))
    //   override fun onError(error: NetworkError) =
    //       crashReporter.recordError(error)

    /** Called immediately before a list load (initial or paginated) begins. */
    protected open fun onLoadStarted() {}

    /** Called after a successful load with the number of items now shown. */
    protected open fun onLoadCompleted(itemCount: Int) {}

    /**
     * Called when any CRUD operation surfaces an error (8C).
     *
     * Default implementation forwards the error to [CrashReporter] so all
     * unhandled ViewModel errors are captured in Sentry without any subclass
     * boilerplate. Override to suppress specific error types or add context:
     *
     * ```kotlin
     * override fun onError(error: NetworkError) {
     *     if (error is NetworkError.Unauthorized) return // handled by auth interceptor
     *     super.onError(error) // still captures everything else
     * }
     * ```
     */
    protected open fun onError(error: NetworkError) {
        if (CrashReporter.isInitialized) {
            CrashReporter.captureException(error)
        }
    }

    // ── Intent handling ───────────────────────────────────────────────────────

    override fun handleIntent(intent: CrudListIntent) {
        when (intent) {
            is CrudListIntent.Load -> loadItems(reset = true)
            is CrudListIntent.Refresh -> refreshItems()
            is CrudListIntent.LoadNextPage -> if (currentState.hasNext) loadItems(reset = false)
            is CrudListIntent.Select -> {
                setState { copy(selectedId = intent.id) }
                sendEffect(CrudListEffect.NavigateToDetail(intent.id))
            }
            is CrudListIntent.Delete -> deleteItem(intent.id)
        }
    }

    private fun loadItems(reset: Boolean) = launch {
        val nextPage = if (reset) 1 else currentState.page + 1
        setState { copy(isLoading = true, error = null) }
        onLoadStarted()

        when (val result = useCases.getAll(page = nextPage)) {
            is Result.Success -> {
                val paginated = result.data
                val merged = if (reset) paginated.data.map { mapToDto(it) }
                             else currentState.items + paginated.data.map { mapToDto(it) }
                setState {
                    copy(
                        items = merged,
                        page = nextPage,
                        hasNext = paginated.hasNext,
                        isLoading = false,
                    )
                }
                onLoadCompleted(merged.size)
            }
            is Result.Error -> {
                setState { copy(isLoading = false, error = result.error) }
                sendEffect(CrudListEffect.ShowError(result.error))
                onError(result.error)
            }
        }
    }

    private fun refreshItems() = launch {
        setState { copy(isRefreshing = true, error = null) }
        onLoadStarted()

        when (val result = useCases.getAll(page = 1)) {
            is Result.Success -> {
                val paginated = result.data
                val items = paginated.data.map { mapToDto(it) }
                setState {
                    copy(
                        items = items,
                        page = 1,
                        hasNext = paginated.hasNext,
                        isRefreshing = false,
                    )
                }
                onLoadCompleted(items.size)
            }
            is Result.Error -> {
                setState { copy(isRefreshing = false, error = result.error) }
                sendEffect(CrudListEffect.ShowError(result.error))
                onError(result.error)
            }
        }
    }

    private fun deleteItem(id: String) = launch {
        when (val result = useCases.delete(id)) {
            is Result.Success -> {
                setState { copy(items = items.filterNot { extractId(it) == id }) }
                sendEffect(CrudListEffect.DeleteSuccess)
            }
            is Result.Error -> {
                sendEffect(CrudListEffect.ShowError(result.error))
                onError(result.error)
            }
        }
    }
}
