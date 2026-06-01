package id.startapp.presentation.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel implementing the MVI (Model-View-Intent) pattern.
 *
 * Implements [InstanceKeeper.Instance] for automatic lifecycle management
 * with Decompose. When a component is removed from the navigation stack,
 * [onDestroy] is called automatically, cancelling all coroutines and
 * closing the effect channel — preventing memory leaks.
 *
 * MVI Pattern:
 * - **State**: Immutable state representing the UI
 * - **Intent**: User actions/intents
 * - **Effect**: One-time events (navigation, toasts, dialogs)
 *
 * @param S The state type
 * @param I The intent type
 * @param E The effect type
 */
abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEffect> : InstanceKeeper.Instance {

    private val viewModelJob = Job()
    protected val viewModelScope: CoroutineScope = CoroutineScope(viewModelJob)

    /**
     * The current UI state.
     */
    private val _state: MutableStateFlow<S> by lazy { MutableStateFlow(initialState()) }
    val state: StateFlow<S> = _state.asStateFlow()

    /**
     * One-time effects (navigation, toasts, etc.).
     */
    private val _effect: Channel<E> = Channel()
    val effect: Flow<E> = _effect.receiveAsFlow()

    /**
     * Provides the initial state.
     */
    protected abstract fun initialState(): S

    /**
     * Handles incoming intents from the UI.
     */
    protected abstract fun handleIntent(intent: I)

    /**
     * Emit a new state.
     */
    protected fun setState(reducer: S.() -> S) {
        _state.value = _state.value.reducer()
    }

    /**
     * Emit a one-time effect.
     */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * Launch a coroutine in the ViewModel scope.
     */
    protected fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(block = block)
    }

    /**
     * Process an intent.
     */
    fun onIntent(intent: I) {
        handleIntent(intent)
    }

    /**
     * Get the current state value (for synchronous access).
     */
    val currentState: S
        get() = _state.value

    /**
     * Called automatically by Decompose's InstanceKeeper when the component
     * is destroyed. Cancels all coroutines and closes the effect channel.
     */
    override fun onDestroy() {
        viewModelJob.cancel()
        _effect.close()
    }

    /**
     * Manual cleanup alias. Prefer using InstanceKeeper for automatic lifecycle.
     */
    fun clear() {
        onDestroy()
    }
}

/**
 * Marker interface for UI state.
 */
interface UiState

/**
 * Marker interface for UI intents (user actions).
 */
interface UiIntent

/**
 * Marker interface for UI effects (one-time events).
 */
interface UiEffect

/**
 * Composable to collect UI state from a StateFlow.
 *
 * @param stateFlow The StateFlow to collect
 * @param onStateChanged Callback when state changes
 */
@Composable
fun <S : UiState> rememberUiState(
    stateFlow: StateFlow<S>,
    onStateChanged: (S) -> Unit = {}
): S {
    val latestState = remember { stateFlow.value }
    var currentState by remember { mutableStateOf(latestState) }

    LaunchedEffect(stateFlow) {
        stateFlow.collect { newState ->
            currentState = newState
            onStateChanged(newState)
        }
    }

    return currentState
}

/**
 * Composable to collect UI effects from a Flow.
 *
 * @param effectFlow The Flow to collect
 * @param onEffect Callback when effect is emitted
 */
@Composable
fun <E : UiEffect> rememberUiEffect(
    effectFlow: Flow<E>,
    onEffect: (E) -> Unit
) {
    LaunchedEffect(effectFlow) {
        effectFlow.collect { effect ->
            onEffect(effect)
        }
    }
}

/**
 * Base state class with common loading/error states.
 */
data class LoadingState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) : UiState
