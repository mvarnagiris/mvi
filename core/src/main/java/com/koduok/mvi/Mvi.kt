@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.koduok.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

abstract class Mvi<Input, State, Effect>(initialState: State, dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate) : CoroutineScope, Closeable {
    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatcher

    private val inputsFlow = MutableSharedFlow<Input>(replay = Int.MAX_VALUE)
    private val stateFlow = MutableStateFlow(initialState)
    private val effectsFlow = MutableSharedFlow<Effect>(extraBufferCapacity = Int.MAX_VALUE)
    private val uniqueJobs by lazy { hashMapOf<Any, Job>() }
    val effects: SharedFlow<Effect> = effectsFlow.asSharedFlow()
    val states: StateFlow<State> = stateFlow.asStateFlow()
    val state: State get() = stateFlow.value

    init {
        launch {
            inputsFlow
                .flatMapConcat { handleInput(it) }
                .collect { setState(it) }
        }
    }

    fun input(input: Input) {
        inputsFlow.tryEmit(input)
    }

    protected fun effect(effect: Effect) {
        if (effectsFlow.subscriptionCount.value > 0) {
            effectsFlow.tryEmit(effect)
        } else {
            launch {
                effectsFlow.subscriptionCount
                    .filter { it > 0 }
                    .take(1)
                    .collect { effectsFlow.tryEmit(effect) }
            }
        }
    }

    protected abstract fun handleInput(input: Input): Flow<State>

    protected fun launchUniqueIfNotRunning(uniqueJobId: Any, block: suspend () -> Unit): Job? {
        val currentJob = uniqueJobs[uniqueJobId]
        return if (currentJob == null || currentJob.isCompleted) launchUnique(uniqueJobId, block) else null
    }

    protected fun launchUnique(uniqueJobId: Any, block: suspend () -> Unit): Job {
        val currentJob = uniqueJobs[uniqueJobId]
        currentJob?.cancel()

        val job = launch { block() }
        job.invokeOnCompletion {
            val jobForUniqueId = uniqueJobs[uniqueJobId]
            if (job == jobForUniqueId)
                uniqueJobs.remove(uniqueJobId)
        }
        uniqueJobs[uniqueJobId] = job
        return job
    }

    protected fun cancelUnique(uniqueJobId: Any) {
        uniqueJobs.remove(uniqueJobId)?.cancel()
    }

    private fun setState(state: State) {
        stateFlow.value = state
    }

    override fun close() {
        coroutineContext.cancel()
        uniqueJobs.clear()
    }
}