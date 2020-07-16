package com.koduok.mvi

import com.koduok.mvi.MviPaging.Input
import com.koduok.mvi.MviPaging.Input.EditItems
import com.koduok.mvi.MviPaging.Input.LoadNextPage
import com.koduok.mvi.MviPaging.Input.Refresh
import com.koduok.mvi.MviPaging.Input.SetFailedNextPage
import com.koduok.mvi.MviPaging.Input.SetLoadedLastPage
import com.koduok.mvi.MviPaging.Input.SetLoadedNextPage
import com.koduok.mvi.MviPaging.Input.SetLoadingNextPage
import com.koduok.mvi.MviPaging.ItemsEdit.Add
import com.koduok.mvi.MviPaging.ItemsEdit.Remove
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

    fun edit(vararg itemsEdit: ItemsEdit<ITEM>) = input(EditItems(itemsEdit.toList()))

    fun refresh() {
        if (state is Refreshing) return
        input(Refresh)
    }

    fun loadNextPage(force: Boolean = false) {
        if (!force && (state is Refreshing || state is LoadingNextPage || state is LoadedLastPage)) return
        input(LoadNextPage)
    }

    override fun handleInput(input: Input): Flow<State<ITEM>> {
        @Suppress("UNCHECKED_CAST")
        return when (input) {
            is Refresh -> doRefresh()
            is LoadNextPage -> doLoadNextPage()
            is SetLoadingNextPage -> flowOf(LoadingNextPage(state.items))
            is SetLoadedNextPage<*> -> flowOf(LoadedNextPage(input.allItems as List<ITEM>, input.loadedPage as List<ITEM>))
            is SetLoadedLastPage<*> -> flowOf(LoadedLastPage(input.allItems as List<ITEM>, input.loadedPage as List<ITEM>))
            is SetFailedNextPage -> flowOf(FailedNextPage(state.items, input.cause))
            is EditItems<*> -> doItemsEdit(input.itemsEdits as List<ItemsEdit<ITEM>>)
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
                when {
                    items.isEmpty() -> emit(Empty())
                    isLastPage(request, page, items) -> emit(LoadedLastPage(items, items))
                    else -> emit(Loaded(items))
                }
            } catch (e: Exception) {
                emit(Failed(state.items, e))
            }
        }
    }

    private fun doLoadNextPage(): Flow<State<ITEM>> {
        if (!canLoadNextPage(state)) return emptyFlow()

        launchUniqueIfNotRunning("next_page") {
            input(SetLoadingNextPage)

            try {
                val request = getRequest(NEXT_PAGE)
                val page = getItems(request)
                val items = pageToItems(request, page)
                if (isLastPage(request, page, items)) input(SetLoadedLastPage(state.items + items, items))
                else input(SetLoadedNextPage(state.items + items, items))
            } catch (e: Exception) {
                input(SetFailedNextPage(e))
            }
        }

        return emptyFlow()
    }

    private fun doItemsEdit(edits: List<ItemsEdit<ITEM>>): Flow<State<ITEM>> = flow {
        val newItems = edits.fold(state.items) { newItems, edit ->
            when (edit) {
                is Add -> newItems.take(edit.position) + edit.items + newItems.takeLast(newItems.size - edit.position)
                is Remove -> newItems.filterNot(edit.condition)
            }
        }
        emit(state.withItems(newItems))
    }

    protected abstract suspend fun getRequest(requestType: RequestType): REQUEST
    protected abstract suspend fun getItems(request: REQUEST): PAGE
    protected abstract suspend fun pageToItems(request: REQUEST, page: PAGE): List<ITEM>

    protected open fun isLastPage(request: REQUEST, page: PAGE, loadedItems: List<ITEM>): Boolean = loadedItems.isEmpty()
    protected open fun canLoadNextPage(state: State<ITEM>) = state.canLoadNextPage

    enum class RequestType { REFRESH, NEXT_PAGE }

    sealed class Input {
        internal object Refresh : Input()
        internal object LoadNextPage : Input()
        internal object SetLoadingNextPage : Input()
        internal data class SetLoadedNextPage<ITEM>(val allItems: List<ITEM>, val loadedPage: List<ITEM>) : Input()
        internal data class SetLoadedLastPage<ITEM>(val allItems: List<ITEM>, val loadedPage: List<ITEM>) : Input()
        internal data class SetFailedNextPage(val cause: Exception) : Input()
        internal data class EditItems<ITEM>(val itemsEdits: List<ItemsEdit<ITEM>>) : Input()
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

        internal fun withItems(items: List<ITEM>): State<ITEM> = when (this) {
            is Idle -> copy(items)
            is Refreshing -> copy(items)
            is Loaded -> copy(items)
            is Failed -> copy(items)
            is Empty -> copy(items)
            is LoadingNextPage -> copy(items)
            is LoadedNextPage -> copy(items)
            is LoadedLastPage -> copy(items)
            is FailedNextPage -> copy(items)
        }

        data class Idle<ITEM>(override val items: List<ITEM> = emptyList()) : State<ITEM>()
        data class Refreshing<ITEM>(override val items: List<ITEM>) : State<ITEM>()
        data class Loaded<ITEM>(override val items: List<ITEM>) : State<ITEM>()
        data class Failed<ITEM>(override val items: List<ITEM>, val cause: Exception) : State<ITEM>()
        data class Empty<ITEM>(override val items: List<ITEM> = emptyList()) : State<ITEM>()
        data class LoadingNextPage<ITEM>(override val items: List<ITEM>) : State<ITEM>()
        data class LoadedNextPage<ITEM>(override val items: List<ITEM>, val page: List<ITEM>) : State<ITEM>()
        data class LoadedLastPage<ITEM>(override val items: List<ITEM>, val page: List<ITEM>) : State<ITEM>()
        data class FailedNextPage<ITEM>(override val items: List<ITEM>, val cause: Exception) : State<ITEM>()
    }

    sealed class ItemsEdit<ITEM> {
        data class Add<ITEM>(val position: Int, val items: List<ITEM>) : ItemsEdit<ITEM>()
        data class Remove<ITEM>(val condition: (ITEM) -> Boolean) : ItemsEdit<ITEM>()
    }
}