package com.koduok.mvi

import com.koduok.mvi.MviPaging.Input
import com.koduok.mvi.MviPaging.Input.LoadNextPage
import com.koduok.mvi.MviPaging.Input.Refresh
import com.koduok.mvi.MviPaging.Input.SetFailedNextPage
import com.koduok.mvi.MviPaging.Input.SetLoadedLastPage
import com.koduok.mvi.MviPaging.Input.SetLoadedNextPage
import com.koduok.mvi.MviPaging.Input.SetLoadingNextPage
import com.koduok.mvi.MviPaging.RequestType.NEXT_PAGE
import com.koduok.mvi.MviPaging.RequestType.REFRESH
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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

abstract class MviPaging<ITEM, REQUEST, PAGE> : Mvi<Input, State<ITEM>>(Idle()) {

    fun refresh() = input(Refresh)
    fun loadNextPage() = input(LoadNextPage)

    override fun handleInput(input: Input): Flow<State<ITEM>> {
        @Suppress("UNCHECKED_CAST")
        return when (input) {
            is Refresh -> doRefresh()
            is LoadNextPage -> doLoadNextPage()
            is SetLoadingNextPage -> flowOf(LoadingNextPage(state.items))
            is SetLoadedNextPage<*> -> flowOf(LoadedNextPage(input.allItems as List<ITEM>, input.loadedPage as List<ITEM>))
            is SetLoadedLastPage -> flowOf(LoadedLastPage(state.items))
            is SetFailedNextPage -> flowOf(FailedNextPage(state.items, input.cause))
        }
    }

    private fun doRefresh(): Flow<State<ITEM>> {
        cancelUnique("next_page")
        return flow {
            emit(Refreshing(state.items))

            try {
                val request = getRequest(REFRESH)
                val page = getItems(request)
                val items = pageToItems(request, page)
                if (items.isEmpty()) emit(Empty())
                else emit(Loaded(items))
            } catch (e: Exception) {
                emit(Failed(state.items, e))
            }
        }
    }

    private fun doLoadNextPage(): Flow<State<ITEM>> {
        if (!state.canLoadNextPage) return emptyFlow()

        launchUniqueIfNotRunning("next_page") {
            input(SetLoadingNextPage)

            try {
                val request = getRequest(NEXT_PAGE)
                val page = getItems(request)
                val items = pageToItems(request, page)
                if (items.isEmpty()) input(SetLoadedLastPage)
                else input(SetLoadedNextPage(state.items + items, items))
            } catch (e: Exception) {
                input(SetFailedNextPage(e))
            }
        }

        return emptyFlow()
    }

    protected abstract suspend fun getRequest(requestType: RequestType): REQUEST
    protected abstract suspend fun getItems(request: REQUEST): PAGE
    protected abstract suspend fun pageToItems(request: REQUEST, page: PAGE): List<ITEM>

    enum class RequestType { REFRESH, NEXT_PAGE }

    sealed class Input {
        internal object Refresh : Input()
        internal object LoadNextPage : Input()
        internal object SetLoadingNextPage : Input()
        internal data class SetLoadedNextPage<ITEM>(val allItems: List<ITEM>, val loadedPage: List<ITEM>) : Input()
        internal object SetLoadedLastPage : Input()
        internal data class SetFailedNextPage(val cause: Exception) : Input()
    }

    sealed class State<ITEM> {
        abstract val items: List<ITEM>
        val canLoadNextPage: Boolean
            get() = when (this) {
                is Idle -> false
                is Refreshing -> false
                is Loaded -> true
                is Failed -> false
                is Empty -> false
                is LoadingNextPage -> false
                is LoadedNextPage -> true
                is LoadedLastPage -> false
                is FailedNextPage -> true
            }

        data class Idle<ITEM>(override val items: List<ITEM> = emptyList()) : State<ITEM>()
        data class Refreshing<ITEM>(override val items: List<ITEM>) : State<ITEM>()
        data class Loaded<ITEM>(override val items: List<ITEM>) : State<ITEM>()
        data class Failed<ITEM>(override val items: List<ITEM>, val cause: Exception) : State<ITEM>()
        data class Empty<ITEM>(override val items: List<ITEM> = emptyList()) : State<ITEM>()
        data class LoadingNextPage<ITEM>(override val items: List<ITEM>) : State<ITEM>()
        data class LoadedNextPage<ITEM>(override val items: List<ITEM>, val page: List<ITEM>) : State<ITEM>()
        data class LoadedLastPage<ITEM>(override val items: List<ITEM>) : State<ITEM>()
        data class FailedNextPage<ITEM>(override val items: List<ITEM>, val cause: Exception) : State<ITEM>()
    }
}