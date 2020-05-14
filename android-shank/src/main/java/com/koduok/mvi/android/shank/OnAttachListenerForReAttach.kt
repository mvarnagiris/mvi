package com.koduok.mvi.android.shank

import android.view.View
import android.view.View.OnAttachStateChangeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class OnAttachListenerForReAttach(private val view: View) : OnAttachStateChangeListener {
    private val onAttachedBlocks = mutableMapOf<Any, () -> Unit>()

    init {
        if (view.isAttachedToWindow) onViewAttachedToWindow(view)
    }

    fun put(key: Any, onAttachedBlock: () -> Unit) {
        onAttachedBlocks[key] = onAttachedBlock
        if (view.isAttachedToWindow) onAttachedBlock()
    }

    override fun onViewAttachedToWindow(view: View) {
        onAttachedBlocks.values.forEach { it() }
    }

    override fun onViewDetachedFromWindow(view: View) {}
}