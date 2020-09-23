package com.koduok.mvi.android

import android.view.View
import com.koduok.mvi.Mvi
import kotlinx.coroutines.flow.collect

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.callbacksOn(view: View, uniqueId: Any, callbacks: MviViewCallbacks<INPUT, STATE, MVI>.() -> Unit) {
    val mviCallbacks = MviViewCallbacks<INPUT, STATE, MVI>()
    callbacks(mviCallbacks)

    val currentOnAttachStateChangeListener = view.getTag(R.id.mvi_view_tag) as? OnAttachListenerForCoroutineScope
    val onAttachStateChangeListener = currentOnAttachStateChangeListener ?: OnAttachListenerForCoroutineScope(view)
    onAttachStateChangeListener.replace(
        key = uniqueId,
        onAttachedBlock = { mviCallbacks.onAttachedBlock?.invoke(this) },
        onDetachedBlock = { mviCallbacks.onDetachedBlock?.invoke(this) },
        collectScopesBlock = { mviCallbacks.collectStatesBlock?.let { block -> states.collect { block(this, it) } } }
    )

    view.setTag(R.id.mvi_view_tag, onAttachStateChangeListener)
    if (currentOnAttachStateChangeListener == null) {
        view.addOnAttachStateChangeListener(onAttachStateChangeListener)
    }
}

fun <INPUT, STATE, MVI : Mvi<INPUT, STATE>> MVI.collectStatesOn(view: View, uniqueId: Any, onState: suspend (MVI, STATE) -> Unit) =
    callbacksOn(view, uniqueId) { collectStates(onState) }