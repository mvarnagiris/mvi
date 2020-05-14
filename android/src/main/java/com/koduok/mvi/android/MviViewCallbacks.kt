package com.koduok.mvi.android

import com.koduok.mvi.Mvi

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