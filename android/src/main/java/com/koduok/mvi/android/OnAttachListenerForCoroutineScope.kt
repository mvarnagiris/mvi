package com.koduok.mvi.android

import android.view.View
import android.view.View.OnAttachStateChangeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class OnAttachListenerForCoroutineScope(view: View) : OnAttachStateChangeListener {
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