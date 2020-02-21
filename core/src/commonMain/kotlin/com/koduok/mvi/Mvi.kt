@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.koduok.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.io.core.Closeable
import kotlin.coroutines.CoroutineContext

abstract class Mvi<INPUT, STATE>(initialState: STATE) : CoroutineScope, Closeable {
    override val coroutineContext: CoroutineContext by lazy { SupervisorJob() + Dispatchers.Main }

    private val inputsChannel by lazy { Channel<INPUT>(Channel.UNLIMITED) }
    private val statesChannel by lazy { ConflatedBroadcastChannel(initialState) }
    private val uniqueJobs by lazy { hashMapOf<Any, Job>() }

    val state: STATE get() = statesChannel.value
    val states: Flow<STATE> get() = statesChannel.asFlow()

    init {
        launch {
            for (input in inputsChannel) {
                handleInput(input).collect { setState(it) }
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
        job.invokeOnCompletion { uniqueJobs.remove(uniqueJobId) }
        uniqueJobs[uniqueJobId] = job
        return job
    }

    protected fun cancelUnique(uniqueJobId: Any) {
        uniqueJobs.remove(uniqueJobId)?.cancel()
    }

    private fun setState(state: STATE) {
        val oldState = this.state
        if (state != oldState) {
            statesChannel.offer(state)
        }
    }

    override fun close() {
        inputsChannel.close()
        statesChannel.close()
        coroutineContext.cancel()
    }
}
