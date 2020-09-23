package com.koduok.mvi.android

import android.view.View
import android.view.View.OnAttachStateChangeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class OnAttachListenerForCoroutineScope(view: View) : OnAttachStateChangeListener {
    private var currentCoroutineScope: CoroutineScope? = null

    private val onAttachedBlocks = mutableMapOf<Any, Pair<suspend () -> Unit, suspend () -> Unit>>()
    private val onDetachedBlocks = mutableMapOf<Any, () -> Unit>()
    private val runningJobs = mutableMapOf<Any, Job>()

    init {
        initializeCoroutineScopeIfAttached(view)
    }

    private fun initializeCoroutineScopeIfAttached(view: View) {
        if (view.isAttachedToWindow) onViewAttachedToWindow(view)
    }

    fun replace(key: Any, onAttachedBlock: suspend () -> Unit, onDetachedBlock: () -> Unit, collectScopesBlock: suspend () -> Unit) {
        runningJobs.remove(key)?.cancel()
        onDetachedBlocks[key] = onDetachedBlock
        val coroutineScope = currentCoroutineScope
        if (coroutineScope != null) {
            runningJobs[key] = coroutineScope.launch {
                onAttachedBlock()
                collectScopesBlock()
            }
        } else {
            onAttachedBlocks[key] = onAttachedBlock to collectScopesBlock
        }
    }

    override fun onViewAttachedToWindow(view: View) {
        val coroutineScope = currentCoroutineScope ?: CoroutineScope(SupervisorJob() + Dispatchers.Main)
        currentCoroutineScope = coroutineScope
        onAttachedBlocks.forEach {
            runningJobs[it.key] = coroutineScope.launch {
                it.value.first()
                it.value.second()
            }
        }
        onAttachedBlocks.clear()
    }

    override fun onViewDetachedFromWindow(view: View) {
        currentCoroutineScope?.cancel()
        currentCoroutineScope = null
        onDetachedBlocks.values.forEach { it() }
        onDetachedBlocks.clear()
    }
}