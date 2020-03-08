package com.koduok.mvi.android

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.koduok.mvi.Mvi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.callbacksOn(view: View, callbacks: MviViewCallbacks<INPUT, STATE, MVI>.() -> Unit) {
    val mviCallbacks = MviViewCallbacks<INPUT, STATE, MVI>()
    callbacks(mviCallbacks)

    val onAttachStateChangeListener = view.getTag(R.id.mvi_view_tag) as? OnAttachListenerForCoroutineScope ?: OnAttachListenerForCoroutineScope(view)
    onAttachStateChangeListener.put(
        onAttachedBlock = { mviCallbacks.onAttachedBlock?.invoke(this) },
        onDetachedBlock = { mviCallbacks.onDetachedBlock?.invoke(this) },
        collectScopesBlock = { mviCallbacks.collectStatesBlock?.let { block -> states.collect { block(this, it) } } }
    )
    view.setTag(R.id.mvi_view_tag, onAttachStateChangeListener)
}

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.collectStatesOn(view: View, onState: suspend (MVI, STATE) -> Unit) =
    callbacksOn(view) { collectStates(onState) }

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.callbacksOn(lifecycleOwner: LifecycleOwner, callbacks: MviLifecycleCallbacks<INPUT, STATE, MVI>.() -> Unit) {
    val mviCallbacks = MviLifecycleCallbacks<INPUT, STATE, MVI>()
    callbacks(mviCallbacks)

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

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.collectStatesOn(lifecycleOwner: LifecycleOwner, onState: suspend (MVI, STATE) -> Unit) =
    collectStatesOnResumeOn(lifecycleOwner, onState)

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.collectStatesOnCreateOn(lifecycleOwner: LifecycleOwner, onState: suspend (MVI, STATE) -> Unit) =
    callbacksOn(lifecycleOwner) { collectStatesOnCreate { mvi, state -> onState(mvi, state) } }

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.collectStatesOnStartOn(lifecycleOwner: LifecycleOwner, onState: suspend (MVI, STATE) -> Unit) =
    callbacksOn(lifecycleOwner) { collectStatesOnStart { mvi, state -> onState(mvi, state) } }

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.collectStatesOnResumeOn(lifecycleOwner: LifecycleOwner, onState: suspend (MVI, STATE) -> Unit) =
    callbacksOn(lifecycleOwner) { collectStatesOnResume { mvi, state -> onState(mvi, state) } }

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

    private val onAttachedBlocks = mutableSetOf<Pair<suspend () -> Unit, suspend () -> Unit>>()
    private val onDetachedBlocks = mutableSetOf<() -> Unit>()

    init {
        if (view.isAttachedToWindow) onViewAttachedToWindow(view)
    }

    fun put(onAttachedBlock: suspend () -> Unit, onDetachedBlock: () -> Unit, collectScopesBlock: suspend () -> Unit) {
        onDetachedBlocks.add(onDetachedBlock)
        val coroutineScope = currentCoroutineScope
        if (coroutineScope != null) {
            coroutineScope.launch {
                onAttachedBlock()
                collectScopesBlock()
            }
        } else {
            onAttachedBlocks.add(onAttachedBlock to collectScopesBlock)
        }
    }

    override fun onViewAttachedToWindow(view: View) {
        val coroutineScope = currentCoroutineScope ?: CoroutineScope(SupervisorJob() + Dispatchers.Main)
        currentCoroutineScope = coroutineScope
        onAttachedBlocks.forEach {
            coroutineScope.launch {
                it.first()
                it.second()
            }
        }
        onAttachedBlocks.clear()
    }

    override fun onViewDetachedFromWindow(view: View) {
        currentCoroutineScope?.cancel()
        currentCoroutineScope = null
        onDetachedBlocks.forEach { it() }
        onDetachedBlocks.clear()
    }
}