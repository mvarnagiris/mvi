package com.koduok.mvi

import com.koduok.mvi.MviConcatPaging.Input
import com.koduok.mvi.MviConcatPaging.Input.SetState
import com.koduok.mvi.MviPaging.State
import com.koduok.mvi.MviPaging.State.Empty
import com.koduok.mvi.MviPaging.State.Failed
import com.koduok.mvi.MviPaging.State.FailedNextPage
import com.koduok.mvi.MviPaging.State.Idle
import com.koduok.mvi.MviPaging.State.Loaded
import com.koduok.mvi.MviPaging.State.LoadedLastPage
import com.koduok.mvi.MviPaging.State.LoadedNextPage
import com.koduok.mvi.MviPaging.State.LoadingNextPage
import com.koduok.mvi.MviPaging.State.Refreshing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MviConcatPaging<ITEM>(
    private val map: (pager: MviPaging<*, *, *>, item: Any?) -> ITEM,
    private vararg val pagers: MviPaging<*, *, *>
) : Mvi<Input, State<ITEM>>(Idle()) {

    private val pagersStatus = pagers.mapIndexed { index, mviPaging -> mviPaging to (index == 0) }.toMap().toMutableMap()
    private val currentPager get() = pagers.last { pagersStatus[it] ?: false }
    private val nextPager get() = pagers.firstOrNull { !(pagersStatus[it] ?: false) }
    private val activePagers get() = pagers.takeWhile { pagersStatus[it] ?: false }
    private val currentItems get() = activePagers.map { pager -> pager.state.items.map { map(pager, it) } }.flatten()

    init {
        pagers.forEach { pager ->

            launch {
                pager.states.collect { pagerState ->

                    when (pagerState) {
                        is Idle -> if (pager.isFirst && pager.isCurrent) input(SetState(Idle<ITEM>()))
                        is Refreshing -> {
                            if (pager.isCurrent) {
                                if (pager.isFirst) input(SetState(Refreshing(currentItems)))
                                else input(SetState(LoadingNextPage(currentItems)))
                            } else {
                                input(SetState(state.withItems(currentItems)))
                            }
                        }
                        is Loaded -> {
                            if (pager.isCurrent) {
                                if (pager.isFirst) input(SetState(Loaded(currentItems)))
                                else input(SetState(LoadedNextPage(currentItems, pagerState.items.map { map(pager, it) })))
                            } else {
                                input(SetState(state.withItems(currentItems)))
                            }
                        }
                        is Failed -> {
                            if (pager.isCurrent) {
                                if (pager.isFirst) input(SetState(Failed(currentItems, pagerState.cause)))
                                else input(SetState(FailedNextPage(currentItems, pagerState.cause)))
                            } else {
                                input(SetState(state.withItems(currentItems)))
                            }
                        }
                        is Empty -> {
                            if (pager.isCurrent) {
                                val items = currentItems
                                when {
                                    pager.isLast && items.isEmpty() -> input(SetState(Empty(items)))
                                    pager.isLast -> input(SetState(LoadedLastPage(items, emptyList())))
                                    else -> {
                                        val nextPager = nextPager!!
                                        pagersStatus[nextPager] = true
                                        nextPager.refresh()
                                    }
                                }
                            } else {
                                input(SetState(state.withItems(currentItems)))
                            }
                        }
                        is LoadingNextPage -> {
                            if (pager.isCurrent) {
                                input(SetState(LoadingNextPage(currentItems)))
                            } else {
                                input(SetState(state.withItems(currentItems)))
                            }
                        }
                        is LoadedNextPage -> {
                            if (pager.isCurrent) {
                                input(SetState(LoadedNextPage(currentItems, pagerState.page.map { map(pager, it) })))
                            } else {
                                input(SetState(state.withItems(currentItems)))
                            }
                        }
                        is LoadedLastPage -> {
                            if (pager.isCurrent) {
                                if (pager.isLast) input(SetState(LoadedLastPage(currentItems, pagerState.page.map { map(pager, it) })))
                                else {
                                    val nextPager = nextPager!!
                                    pagersStatus[nextPager] = true
                                    nextPager.refresh()
                                }
                            } else {
                                input(SetState(state.withItems(currentItems)))
                            }
                        }
                        is FailedNextPage -> {
                            if (pager.isCurrent) input(SetState(FailedNextPage(currentItems, pagerState.cause)))
                        }
                    }

                }
            }
        }
    }

    fun refresh() {
        if (state is Refreshing) return

        pagers.forEachIndexed { index, pager -> pagersStatus[pager] = index == 0 }
        currentPager.refresh()
    }

    fun loadNextPage(force: Boolean = false) {
        if (!force && (state is Refreshing || state is LoadingNextPage || state is LoadedLastPage)) return

        currentPager.loadNextPage(force)
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleInput(input: Input): Flow<State<ITEM>> = when (input) {
        is SetState<*> -> flowOf(input.state as State<ITEM>)
    }

    private val MviPaging<*, *, *>.isFirst get() = pagers.first() == this
    private val MviPaging<*, *, *>.isLast get() = pagers.last() == this
    private val MviPaging<*, *, *>.isCurrent get() = currentPager == this

    sealed class Input {
        internal data class SetState<ITEM>(val state: State<ITEM>) : Input()
    }
}