package com.koduok.mvi.android

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.koduok.mvi.Mvi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


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
