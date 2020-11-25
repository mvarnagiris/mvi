@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.koduok.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

abstract class Mvi<INPUT, STATE>(initialState: STATE, dispatcher: CoroutineDispatcher = Dispatchers.Main) : CoroutineScope, Closeable {
    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatcher

    private val inputsChannel by lazy { Channel<INPUT>(Channel.UNLIMITED) }
    private val uniqueJobs by lazy { hashMapOf<Any, Job>() }

    private val stateFlow = MutableStateFlow(initialState)
    val states: StateFlow<STATE> get() = stateFlow
    val state: STATE get() = stateFlow.value

    init {
        launch {
            for (input in inputsChannel) {
                if (coroutineContext.isActive)
                    handleInput(input).collect { setState(it) }
                else
                    break
            }
        }
    }

    fun input(input: INPUT): Boolean {
        if (inputsChannel.isClosedForSend) return false

        inputsChannel.offer(input)
        return true
    }

    protected abstract fun handleInput(input: INPUT): Flow<STATE>

    protected fun launchUniqueIfNotRunning(uniqueJobId: Any, block: suspend () -> Unit): Job? {
        return if (uniqueJobs[uniqueJobId] == null) launchUnique(uniqueJobId, block) else null
    }

    protected fun launchUnique(uniqueJobId: Any, block: suspend () -> Unit): Job {
        val currentJob = uniqueJobs[uniqueJobId]
        currentJob?.cancel()

        val job = launch { block() }
        job.invokeOnCompletion { if (uniqueJobs[uniqueJobId] == job) uniqueJobs.remove(uniqueJobId) }
        uniqueJobs[uniqueJobId] = job
        return job
    }

    protected fun cancelUnique(uniqueJobId: Any) {
        uniqueJobs.remove(uniqueJobId)?.cancel()
    }

    private fun setState(state: STATE) {
        stateFlow.value = state
    }

    override fun close() {
        coroutineContext.cancel()
        inputsChannel.close()
    }
}