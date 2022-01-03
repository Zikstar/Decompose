package com.arkivanov.sample.counter.shared

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.Router
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.pop
import com.arkivanov.decompose.router.push
import kotlinx.browser.window
import org.w3c.dom.History

// TODO: test external links and going back
actual fun <C : Any> Router<C, *>.manageBrowserHistory(
    getInfo: (stack: List<C>) -> PageInfo
) {
    // TODO: Avoid initial entry?
    val history = BrowserHistory(HistoryEntry(getInfo(state.value.getStack()), state.value.activeChild.configuration))
    var oldStack = state.value.getStack()
    var skip = true

    state.subscribe { newState ->
        if (skip) {
            skip = false
            return@subscribe
        }

        val newStack = newState.getStack()
        val newInfo = getInfo(newStack)
        val newEntry = HistoryEntry(newInfo, newState.activeChild.configuration)

        if (newStack.dropLast(1) == oldStack) {
            // TODO: push multiple
            history.push(newEntry)
        } else {
            history.replace(newEntry)
        }

        console.log(history)

        oldStack = newStack
    }

    // TODO: unsubscribe?
    window.onpopstate =
        { event ->
            console.log(event.state)

            val newEntry: HistoryEntry<C>? = event.state?.unsafeCast<HistoryEntry<C>>()

            if (newEntry != null) {
                val newIndex = history.stack.indexOfFirst { it.key == newEntry.key }

                if (newIndex == history.index + 1) {
                    console.log("Forward clicked")
                    history.forward()
                    skip = true
                    push(history.stack[history.index].configuration)
                } else if (newIndex == history.index - 1) {
                    val currentState = state.value
                    console.log("Back clicked")
                    history.back()

                    if (history.stack[history.index].configuration == currentState.backStack.lastOrNull()?.configuration) {
                        skip = true
                        pop()
                    } else {
//                        history.replace(HistoryEntry(getInfo(currentState.getStack()), currentState.activeChild.configuration))
                        // TODO: Prevent going forward?
                    }
                } else {
                    error("Illegal new history index: $newIndex. History: ${history.index}, ${history.stack}.")
                }

                console.log(history)
            }
        }
}

private class BrowserHistory<C : Any>(initialEntry: HistoryEntry<C>) {

    private val _stack = ArrayList<HistoryEntry<C>>()
    val stack: List<HistoryEntry<C>> = _stack

    var index: Int = 0
        private set

    init {
        _stack += initialEntry
        window.history.replaceState(initialEntry)
    }

    fun push(entry: HistoryEntry<C>) {
        window.history.pushState(entry)

        while (_stack.lastIndex > index) {
            _stack.removeLast()
        }
        _stack += entry
        index++
    }

    fun replace(entry: HistoryEntry<C>) {
        check(index >= 0)
        window.history.replaceState(entry)
        _stack[index] = entry
    }

    fun forward() {
        check(index < _stack.lastIndex)
        index++
    }

    fun back() {
        check(index > 0)
        index--
    }

    override fun toString(): String =
        "BrowserHistory {index: $index, stack: $_stack}"
}

private data class HistoryEntry<out C : Any>(
    val pageInfo: PageInfo, // TODO: do we need to remember pageInfo here?
    val configuration: C,
) {
    val key: Int = configuration.hashCode()
}

private fun History.pushState(entry: HistoryEntry<*>) {
    console.log("pushState")
    console.log(entry)
    pushState(data = entry, title = entry.pageInfo.title ?: "", url = entry.pageInfo.url)
}

private fun History.replaceState(entry: HistoryEntry<*>) {
    console.log("replaceState")
    console.log(entry)
    replaceState(data = entry, title = entry.pageInfo.title ?: "", url = entry.pageInfo.url)
}

private fun History.getPageInfo(): PageInfo? =
    state?.unsafeCast<PageInfo>()

private fun <C : Any> RouterState<C, *>.getStack(): List<C> =
    backStack.map(Child<C, *>::configuration) + activeChild.configuration
