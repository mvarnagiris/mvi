package com.koduok.mvi.android

import com.koduok.mvi.Mvi

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