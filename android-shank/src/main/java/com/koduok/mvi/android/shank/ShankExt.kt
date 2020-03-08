package com.koduok.mvi.android.shank

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.koduok.mvi.Mvi
import com.koduok.mvi.android.MviLifecycleCallbacks
import com.koduok.mvi.android.MviViewCallbacks
import com.koduok.mvi.android.callbacksOn
import com.koduok.mvi.android.collectStatesOn
import com.koduok.mvi.android.collectStatesOnCreateOn
import com.koduok.mvi.android.collectStatesOnResumeOn
import com.koduok.mvi.android.collectStatesOnStartOn
import life.shank.ScopedProvider0
import life.shank.ScopedProvider1
import life.shank.ScopedProvider2
import life.shank.ScopedProvider3
import life.shank.ScopedProvider4
import life.shank.ScopedProvider5
import life.shank.android.AutoScoped
import life.shank.android.onReadyFor

// SHANK + MVI ScopedProvider0 ---------------------------------------------------------------------------------------------------------------------------------

fun <INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider0<MVI>.callbacksOn(
    view: VIEW,
    callbacks: MviViewCallbacks<INPUT, STATE, MVI>.() -> Unit
) where VIEW : View, VIEW : AutoScoped = onReadyFor(view) { it.callbacksOn(view, callbacks) }

fun <INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider0<MVI>.collectStatesOn(
    view: VIEW,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : View, VIEW : AutoScoped = onReadyFor(view) { it.collectStatesOn(view, onState) }

fun <INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider0<MVI>.callbacksOn(
    view: VIEW,
    callbacks: MviLifecycleCallbacks<INPUT, STATE, MVI>.() -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view) { it.callbacksOn(view, callbacks) }

fun <INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider0<MVI>.collectStatesOn(
    view: VIEW,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view) { it.collectStatesOn(view, onState) }

fun <INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider0<MVI>.collectStatesOnCreateOn(
    view: VIEW,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view) { it.collectStatesOnCreateOn(view, onState) }

fun <INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider0<MVI>.collectStatesOnStartOn(
    view: VIEW,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view) { it.collectStatesOnStartOn(view, onState) }

fun <INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider0<MVI>.collectStatesOnResumeOn(
    view: VIEW,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view) { it.collectStatesOnResumeOn(view, onState) }

// SHANK + MVI ScopedProvider1 ---------------------------------------------------------------------------------------------------------------------------------

fun <P1, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider1<P1, MVI>.callbacksOn(
    view: VIEW,
    p1: P1,
    callbacks: MviViewCallbacks<INPUT, STATE, MVI>.() -> Unit
) where VIEW : View, VIEW : AutoScoped = onReadyFor(view, p1) { it.callbacksOn(view, callbacks) }

fun <P1, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider1<P1, MVI>.collectStatesOn(
    view: VIEW,
    p1: P1,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : View, VIEW : AutoScoped = onReadyFor(view, p1) { it.collectStatesOn(view, onState) }

fun <P1, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider1<P1, MVI>.callbacksOn(
    view: VIEW,
    p1: P1,
    callbacks: MviLifecycleCallbacks<INPUT, STATE, MVI>.() -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1) { it.callbacksOn(view, callbacks) }

fun <P1, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider1<P1, MVI>.collectStatesOn(
    view: VIEW,
    p1: P1,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1) { it.collectStatesOn(view, onState) }

fun <P1, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider1<P1, MVI>.collectStatesOnCreateOn(
    view: VIEW,
    p1: P1,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1) { it.collectStatesOnCreateOn(view, onState) }

fun <P1, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider1<P1, MVI>.collectStatesOnStartOn(
    view: VIEW,
    p1: P1,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1) { it.collectStatesOnStartOn(view, onState) }

fun <P1, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider1<P1, MVI>.collectStatesOnResumeOn(
    view: VIEW,
    p1: P1,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1) { it.collectStatesOnResumeOn(view, onState) }

// SHANK + MVI ScopedProvider2 ---------------------------------------------------------------------------------------------------------------------------------

fun <P1, P2, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider2<P1, P2, MVI>.callbacksOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    callbacks: MviViewCallbacks<INPUT, STATE, MVI>.() -> Unit
) where VIEW : View, VIEW : AutoScoped = onReadyFor(view, p1, p2) { it.callbacksOn(view, callbacks) }

fun <P1, P2, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider2<P1, P2, MVI>.collectStatesOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : View, VIEW : AutoScoped = onReadyFor(view, p1, p2) { it.collectStatesOn(view, onState) }

fun <P1, P2, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider2<P1, P2, MVI>.callbacksOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    callbacks: MviLifecycleCallbacks<INPUT, STATE, MVI>.() -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2) { it.callbacksOn(view, callbacks) }

fun <P1, P2, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider2<P1, P2, MVI>.collectStatesOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2) { it.collectStatesOn(view, onState) }

fun <P1, P2, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider2<P1, P2, MVI>.collectStatesOnCreateOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2) { it.collectStatesOnCreateOn(view, onState) }

fun <P1, P2, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider2<P1, P2, MVI>.collectStatesOnStartOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2) { it.collectStatesOnStartOn(view, onState) }

fun <P1, P2, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider2<P1, P2, MVI>.collectStatesOnResumeOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2) { it.collectStatesOnResumeOn(view, onState) }

// SHANK + MVI ScopedProvider3 ---------------------------------------------------------------------------------------------------------------------------------

fun <P1, P2, P3, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider3<P1, P2, P3, MVI>.callbacksOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    callbacks: MviViewCallbacks<INPUT, STATE, MVI>.() -> Unit
) where VIEW : View, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3) { it.callbacksOn(view, callbacks) }

fun <P1, P2, P3, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider3<P1, P2, P3, MVI>.collectStatesOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : View, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3) { it.collectStatesOn(view, onState) }

fun <P1, P2, P3, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider3<P1, P2, P3, MVI>.callbacksOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    callbacks: MviLifecycleCallbacks<INPUT, STATE, MVI>.() -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3) { it.callbacksOn(view, callbacks) }

fun <P1, P2, P3, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider3<P1, P2, P3, MVI>.collectStatesOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3) { it.collectStatesOn(view, onState) }

fun <P1, P2, P3, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider3<P1, P2, P3, MVI>.collectStatesOnCreateOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3) { it.collectStatesOnCreateOn(view, onState) }

fun <P1, P2, P3, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider3<P1, P2, P3, MVI>.collectStatesOnStartOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3) { it.collectStatesOnStartOn(view, onState) }

fun <P1, P2, P3, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider3<P1, P2, P3, MVI>.collectStatesOnResumeOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3) { it.collectStatesOnResumeOn(view, onState) }

// SHANK + MVI ScopedProvider4 ---------------------------------------------------------------------------------------------------------------------------------

fun <P1, P2, P3, P4, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider4<P1, P2, P3, P4, MVI>.callbacksOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    callbacks: MviViewCallbacks<INPUT, STATE, MVI>.() -> Unit
) where VIEW : View, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3, p4) { it.callbacksOn(view, callbacks) }

fun <P1, P2, P3, P4, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider4<P1, P2, P3, P4, MVI>.collectStatesOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : View, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3, p4) { it.collectStatesOn(view, onState) }

fun <P1, P2, P3, P4, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider4<P1, P2, P3, P4, MVI>.callbacksOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    callbacks: MviLifecycleCallbacks<INPUT, STATE, MVI>.() -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3, p4) { it.callbacksOn(view, callbacks) }

fun <P1, P2, P3, P4, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider4<P1, P2, P3, P4, MVI>.collectStatesOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3, p4) { it.collectStatesOn(view, onState) }

fun <P1, P2, P3, P4, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider4<P1, P2, P3, P4, MVI>.collectStatesOnCreateOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3, p4) { it.collectStatesOnCreateOn(view, onState) }

fun <P1, P2, P3, P4, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider4<P1, P2, P3, P4, MVI>.collectStatesOnStartOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3, p4) { it.collectStatesOnStartOn(view, onState) }

fun <P1, P2, P3, P4, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider4<P1, P2, P3, P4, MVI>.collectStatesOnResumeOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3, p4) { it.collectStatesOnResumeOn(view, onState) }

// SHANK + MVI ScopedProvider5 ---------------------------------------------------------------------------------------------------------------------------------

fun <P1, P2, P3, P4, P5, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider5<P1, P2, P3, P4, P5, MVI>.callbacksOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    p5: P5,
    callbacks: MviViewCallbacks<INPUT, STATE, MVI>.() -> Unit
) where VIEW : View, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3, p4, p5) { it.callbacksOn(view, callbacks) }

fun <P1, P2, P3, P4, P5, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider5<P1, P2, P3, P4, P5, MVI>.collectStatesOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    p5: P5,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : View, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3, p4, p5) { it.collectStatesOn(view, onState) }

fun <P1, P2, P3, P4, P5, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider5<P1, P2, P3, P4, P5, MVI>.callbacksOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    p5: P5,
    callbacks: MviLifecycleCallbacks<INPUT, STATE, MVI>.() -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3, p4, p5) { it.callbacksOn(view, callbacks) }

fun <P1, P2, P3, P4, P5, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider5<P1, P2, P3, P4, P5, MVI>.collectStatesOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    p5: P5,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3, p4, p5) { it.collectStatesOn(view, onState) }

fun <P1, P2, P3, P4, P5, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider5<P1, P2, P3, P4, P5, MVI>.collectStatesOnCreateOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    p5: P5,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3, p4, p5) { it.collectStatesOnCreateOn(view, onState) }

fun <P1, P2, P3, P4, P5, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider5<P1, P2, P3, P4, P5, MVI>.collectStatesOnStartOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    p5: P5,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3, p4, p5) { it.collectStatesOnStartOn(view, onState) }

fun <P1, P2, P3, P4, P5, INPUT, STATE, VIEW, MVI : Mvi<INPUT, STATE>> ScopedProvider5<P1, P2, P3, P4, P5, MVI>.collectStatesOnResumeOn(
    view: VIEW,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    p5: P5,
    onState: suspend (MVI, STATE) -> Unit
) where VIEW : LifecycleOwner, VIEW : AutoScoped = onReadyFor(view, p1, p2, p3, p4, p5) { it.collectStatesOnResumeOn(view, onState) }
