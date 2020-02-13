package com.koduok.mvi.android

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.koduok.mvi.Mvi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Inside a view:
 * ```
 * mvi.callbacksOn(this) {
 *     // This can be called multiple times on a view, because it can be attached/detached multiple times. Most commonly in RecyclerView.
 *     onAttached { mvi ->
 *         // Suspend function that will finish executing before collectStates starts.
 *     }
 *
 *     collectStates { mvi, state ->
 *         // Suspend function called every time state changes and once initially. Here you should be updating your view
 *     }
 *
 *     // This can be called multiple times on a view, because it can be attached/detached multiple times. Most commonly in RecyclerView.
 *     onDetached { mvi ->
 *         // Simple function that is called when view is detached and CoroutineScope is destroyed.
 *     }
 * }
 * ```
 * If you have more than one callbacksOn block on a view, make sure to pass uniqueId for each block, otherwise only the last one will be registered.
 */
fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.callbacksOn(view: View, uniqueId: Any = "", callbacks: MviViewCallbacks<INPUT, STATE, MVI>.() -> Unit) {
    val mviCallbacks = MviViewCallbacks<INPUT, STATE, MVI>()
    callbacks(mviCallbacks)

    val onAttachStateChangeListener = view.getTag(R.id.mvi_view_tag) as? OnAttachListenerForCoroutineScope ?: OnAttachListenerForCoroutineScope(view)
    onAttachStateChangeListener.put(
        uniqueId,
        onAttachedBlock = { mviCallbacks.onAttachedBlock?.invoke(this) },
        onDetachedBlock = { mviCallbacks.onDetachedBlock?.invoke(this) },
        collectScopesBlock = { mviCallbacks.collectStatesBlock?.let { block -> states.collect { block(this, it) } } }
    )
    view.setTag(R.id.mvi_view_tag, onAttachStateChangeListener)
}

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.collectStatesOn(view: View, uniqueId: Any = "", onState: suspend (MVI, STATE) -> Unit) =
    callbacksOn(view, uniqueId) {
        collectStates(onState)
    }

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.callbacksOn(lifecycleOwner: LifecycleOwner, callbacks: MviLifecycleCallbacks<INPUT, STATE, MVI>.() -> Unit) {
    val mviCallbacks = MviLifecycleCallbacks<INPUT, STATE, MVI>()
    callbacks(mviCallbacks)

    if (mviCallbacks.hasAnyLifecycleCallbacks) {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {

            private var onCreateJob: Job? = null
            private var onStartJob: Job? = null
            private var onResumeJob: Job? = null

            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                @Suppress("NON_EXHAUSTIVE_WHEN")
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        mviCallbacks.onCreateBlock?.invoke(this@callbacksOn)
                        onCreateJob = mviCallbacks.collectStatesOnCreateBlock?.let { block ->
                            lifecycleOwner.lifecycle.coroutineScope.launch { states.collect { block(this@callbacksOn, it) } }
                        }
                    }
                    Lifecycle.Event.ON_START -> {
                        mviCallbacks.onStartBlock?.invoke(this@callbacksOn)
                        onStartJob = mviCallbacks.collectStatesOnStartBlock?.let { block ->
                            lifecycleOwner.lifecycle.coroutineScope.launch { states.collect { block(this@callbacksOn, it) } }
                        }
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        mviCallbacks.onResumeBlock?.invoke(this@callbacksOn)
                        onResumeJob = mviCallbacks.collectStatesOnResumeBlock?.let { block ->
                            lifecycleOwner.lifecycle.coroutineScope.launch { states.collect { block(this@callbacksOn, it) } }
                        }
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        onResumeJob?.cancel()
                        onResumeJob = null
                        mviCallbacks.onPauseBlock?.invoke(this@callbacksOn)
                    }
                    Lifecycle.Event.ON_STOP -> {
                        onStartJob?.cancel()
                        onStartJob = null
                        mviCallbacks.onStopBlock?.invoke(this@callbacksOn)
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        onCreateJob?.cancel()
                        onCreateJob = null
                        mviCallbacks.onDestroyBlock?.invoke(this@callbacksOn)
                    }
                }
            }
        })
    }
}

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.collectStatesOn(lifecycleOwner: LifecycleOwner, onState: suspend (MVI, STATE) -> Unit) =
    collectStatesOnResumeOn(lifecycleOwner, onState)

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.collectStatesOnCreateOn(lifecycleOwner: LifecycleOwner, onState: suspend (MVI, STATE) -> Unit) {
    callbacksOn(lifecycleOwner) {
        collectStatesOnCreate { mvi, state -> onState(mvi, state) }
    }
}

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.collectStatesOnStartOn(lifecycleOwner: LifecycleOwner, onState: suspend (MVI, STATE) -> Unit) {
    callbacksOn(lifecycleOwner) {
        collectStatesOnStart { mvi, state -> onState(mvi, state) }
    }
}

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.collectStatesOnResumeOn(lifecycleOwner: LifecycleOwner, onState: suspend (MVI, STATE) -> Unit) {
    callbacksOn(lifecycleOwner) {
        collectStatesOnResume { mvi, state -> onState(mvi, state) }
    }
}

class MviViewCallbacks<INPUT, STATE, MVI : Mvi<INPUT, STATE>> {
    internal var onAttachedBlock: ((MVI) -> Unit)? = null
    internal var onDetachedBlock: ((MVI) -> Unit)? = null
    internal var collectStatesBlock: (suspend (MVI, STATE) -> Unit)? = null

    fun onAttached(block: (MVI) -> Unit) {
        onAttachedBlock = block
    }

    fun onDetached(block: (MVI) -> Unit) {
        onDetachedBlock = block
    }

    fun collectStates(block: suspend (MVI, STATE) -> Unit) {
        collectStatesBlock = block
    }
}

class MviLifecycleCallbacks<INPUT, STATE, MVI : Mvi<INPUT, STATE>> {
    internal var onCreateBlock: ((MVI) -> Unit)? = null
    internal var onStartBlock: ((MVI) -> Unit)? = null
    internal var onResumeBlock: ((MVI) -> Unit)? = null
    internal var onPauseBlock: ((MVI) -> Unit)? = null
    internal var onStopBlock: ((MVI) -> Unit)? = null
    internal var onDestroyBlock: ((MVI) -> Unit)? = null
    internal var collectStatesOnCreateBlock: (suspend (MVI, STATE) -> Unit)? = null
    internal var collectStatesOnStartBlock: (suspend (MVI, STATE) -> Unit)? = null
    internal var collectStatesOnResumeBlock: (suspend (MVI, STATE) -> Unit)? = null

    internal val hasAnyLifecycleCallbacks
        get() =
            onCreateBlock != null || onStartBlock != null || onResumeBlock != null || onPauseBlock != null || onStopBlock != null || onDestroyBlock != null

    fun onCreate(block: (MVI) -> Unit) {
        onCreateBlock = block
    }

    fun onStart(block: (MVI) -> Unit) {
        onStartBlock = block
    }

    fun onResume(block: (MVI) -> Unit) {
        onResumeBlock = block
    }

    fun onPause(block: (MVI) -> Unit) {
        onPauseBlock = block
    }

    fun onStop(block: (MVI) -> Unit) {
        onStopBlock = block
    }

    fun onDestroy(block: (MVI) -> Unit) {
        onDestroyBlock = block
    }

    fun collectStatesOnCreate(block: suspend (MVI, STATE) -> Unit) {
        collectStatesOnCreateBlock = block
    }

    fun collectStatesOnStart(block: suspend (MVI, STATE) -> Unit) {
        collectStatesOnStartBlock = block
    }

    fun collectStatesOnResume(block: suspend (MVI, STATE) -> Unit) {
        collectStatesOnResumeBlock = block
    }
}

private class OnAttachListenerForCoroutineScope(view: View) : View.OnAttachStateChangeListener {
    private var currentCoroutineScope: CoroutineScope? = null

    private val onAttachedBlocks = hashMapOf<Any, suspend () -> Unit>()
    private val onDetachedBlocks = hashMapOf<Any, () -> Unit>()
    private val collectScopesBlocks = hashMapOf<Any, suspend () -> Unit>()
    private val runningJobs = hashMapOf<Any, Job>()
    private val runningCompletions = hashMapOf<Any, DisposableHandle>()

    init {
        if (view.isAttachedToWindow) onViewAttachedToWindow(view)
    }

    fun put(
        uniqueId: Any,
        onAttachedBlock: suspend () -> Unit,
        onDetachedBlock: () -> Unit,
        collectScopesBlock: suspend () -> Unit
    ) {
        onAttachedBlocks[uniqueId] = onAttachedBlock
        onDetachedBlocks[uniqueId] = onDetachedBlock
        collectScopesBlocks[uniqueId] = collectScopesBlock

        onAttached(currentCoroutineScope, uniqueId, onAttachedBlock, collectScopesBlock)
    }

    override fun onViewAttachedToWindow(view: View) {
        val coroutineScope = currentCoroutineScope ?: CoroutineScope(SupervisorJob() + Dispatchers.Main)
        currentCoroutineScope = coroutineScope
        collectScopesBlocks.forEach {
            onAttached(coroutineScope, it.key, onAttachedBlocks[it.key]!!, it.value)
        }
    }

    private fun onAttached(
        coroutineScope: CoroutineScope?,
        uniqueId: Any,
        onAttachedBlock: suspend () -> Unit,
        collectScopesBlock: suspend () -> Unit
    ) {
        runningCompletions.remove(uniqueId)?.dispose()
        runningJobs.remove(uniqueId)?.cancel()
        val job = coroutineScope?.launch {
            onAttachedBlock()
            collectScopesBlock()
        }
        val completion = job?.invokeOnCompletion {
            runningJobs.remove(uniqueId)
            runningCompletions.remove(uniqueId)?.dispose()
        }
        if (job != null) runningJobs[uniqueId] = job
        if (completion != null) runningCompletions[uniqueId] = completion
    }

    override fun onViewDetachedFromWindow(view: View) {
        currentCoroutineScope?.cancel()
        currentCoroutineScope = null
        runningJobs.clear()
        runningCompletions.clear()
        onDetachedBlocks.values.forEach { it() }
    }
}
