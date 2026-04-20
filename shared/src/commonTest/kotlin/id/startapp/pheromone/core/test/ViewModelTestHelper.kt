package id.startapp.pheromone.core.test

import id.startapp.pheromone.presentation.viewmodel.BaseViewModel
import id.startapp.pheromone.presentation.viewmodel.UiEffect
import id.startapp.pheromone.presentation.viewmodel.UiIntent
import id.startapp.pheromone.presentation.viewmodel.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

/**
 * Test helpers for [BaseViewModel] subclasses.
 *
 * ## Usage
 * ```kotlin
 * class OrderListViewModelTest {
 *     private val fakeRepo = FakeCrudRepository<Order>(idExtractor = { it.id })
 *     private val vm = OrderListViewModel(fakeRepo.toUseCases())
 *
 *     @Test
 *     fun `initial state is empty and not loading`() {
 *         assertInitialState(vm) { state ->
 *             assertFalse(state.isLoading)
 *             assertTrue(state.items.isEmpty())
 *         }
 *     }
 *
 *     @Test
 *     fun `Load populates items`() = runTest {
 *         fakeRepo.items += Order(id = "1", name = "Test")
 *         val states = collectStates(vm) {
 *             vm.onIntent(CrudListIntent.Load)
 *         }
 *         assertTrue(states.last().items.isNotEmpty())
 *     }
 * }
 * ```
 */
object ViewModelTestHelper {

    /**
     * Assert on the initial state of a ViewModel synchronously (no coroutine needed).
     */
    fun <S : UiState, I : UiIntent, E : UiEffect> assertInitialState(
        viewModel: BaseViewModel<S, I, E>,
        block: (S) -> Unit,
    ) {
        block(viewModel.currentState)
    }

    /**
     * Collect all state emissions produced while [block] runs.
     * Returns the list of states emitted (including intermediate ones).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <S : UiState, I : UiIntent, E : UiEffect> TestScope.collectStates(
        viewModel: BaseViewModel<S, I, E>,
        block: suspend () -> Unit,
    ): List<S> {
        val states = mutableListOf<S>()
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(states)
        }
        runTest { block() }
        job.cancel()
        return states
    }

    /**
     * Collect effect emissions produced while [block] runs.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <S : UiState, I : UiIntent, Eff : UiEffect> TestScope.collectEffects(
        viewModel: BaseViewModel<S, I, Eff>,
        block: suspend () -> Unit,
    ): List<Eff> {
        val effects = mutableListOf<Eff>()
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.effects.toList(effects)
        }
        runTest { block() }
        job.cancel()
        return effects
    }
}

/**
 * Convenience extension — send an intent and return the resulting state.
 */
fun <S : UiState, I : UiIntent, E : UiEffect> BaseViewModel<S, I, E>.onIntentAndGetState(
    intent: I,
): S {
    onIntent(intent)
    return currentState
}
