package com.koduok.mvi.android

import android.view.View

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

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.collectStatesOn(view: View, uniqueId: Any = "", onState: suspend (MVI, STATE) -> Unit) {
    callbacksOn(view, uniqueId) {
        collectStates(onState)
    }
}

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.callbacksOn(lifecycleOwner: LifecycleOwner, callbacks: MviLifecycleCallbacks<INPUT, STATE, MVI>.() -> Unit) {
    val mviCallbacks = MviLifecycleCallbacks<INPUT, STATE, MVI>()
    callbacks(mviCallbacks)

    if (mviCallbacks.hasAnyLifecycleCallbacks) {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {

            private var onCreateJob: Job? = null
            private var onStartJob: Job? = null
            private var onResumeJob: Job? = null

            override fun onStateChanged(source: LifecycleOwner, event: Event) {
                @Suppress("NON_EXHAUSTIVE_WHEN")
                when (event) {
                    ON_CREATE -> {
                        mviCallbacks.onCreateBlock?.invoke(this@callbacksOn)
                        onCreateJob = mviCallbacks.collectStatesOnCreateBlock?.let { block ->
                            lifecycleOwner.lifecycle.coroutineScope.launch { states.collect { block(this@callbacksOn, it) } }
                        }
                    }
                    ON_START -> {
                        mviCallbacks.onStartBlock?.invoke(this@callbacksOn)
                        onStartJob = mviCallbacks.collectStatesOnStartBlock?.let { block ->
                            lifecycleOwner.lifecycle.coroutineScope.launch { states.collect { block(this@callbacksOn, it) } }
                        }
                    }
                    ON_RESUME -> {
                        mviCallbacks.onResumeBlock?.invoke(this@callbacksOn)
                        onResumeJob = mviCallbacks.collectStatesOnResumeBlock?.let { block ->
                            lifecycleOwner.lifecycle.coroutineScope.launch { states.collect { block(this@callbacksOn, it) } }
                        }
                    }
                    ON_PAUSE -> {
                        onResumeJob?.cancel()
                        onResumeJob = null
                        mviCallbacks.onPauseBlock?.invoke(this@callbacksOn)
                    }
                    ON_STOP -> {
                        onStartJob?.cancel()
                        onStartJob = null
                        mviCallbacks.onStopBlock?.invoke(this@callbacksOn)
                    }
                    ON_DESTROY -> {
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

private class OnAttachListenerForCoroutineScope(view: View) : OnAttachStateChangeListener {
    private var currentCoroutineScope: CoroutineScope? = null

    private val onAttachedBlocks = hashMapOf<Any, () -> Unit>()
    private val onDetachedBlocks = hashMapOf<Any, () -> Unit>()
    private val collectScopesBlocks = hashMapOf<Any, suspend () -> Unit>()
    private val runningJobs = hashMapOf<Any, Job>()

    init {
        if (view.isAttachedToWindow) onViewAttachedToWindow(view)
    }

    fun put(
        uniqueId: Any,
        onAttachedBlock: () -> Unit,
        onDetachedBlock: () -> Unit,
        collectScopesBlock: suspend () -> Unit
    ) {
        runningJobs.remove(uniqueId)?.cancel()
        onAttachedBlocks[uniqueId] = onAttachedBlock
        onDetachedBlocks[uniqueId] = onDetachedBlock
        collectScopesBlocks[uniqueId] = collectScopesBlock

        val coroutineScope = currentCoroutineScope
        if (coroutineScope != null) {
            onAttachedBlock()
            coroutineScope.launch { collectScopesBlock() }
        }
    }

    override fun onViewAttachedToWindow(view: View) {
        val coroutineScope = currentCoroutineScope ?: CoroutineScope(SupervisorJob() + Dispatchers.Main)
        currentCoroutineScope = coroutineScope

        onAttachedBlocks.values.forEach { it() }
        collectScopesBlocks.forEach { runningJobs[it.key] = coroutineScope.launch { it.value() } }
    }

    override fun onViewDetachedFromWindow(view: View) {
        currentCoroutineScope?.cancel()
        currentCoroutineScope = null
        runningJobs.clear()
        onDetachedBlocks.values.forEach { it() }
    }
}