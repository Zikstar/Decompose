package com.arkivanov.sample.counter.shared

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.Router
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.pop
import kotlinx.browser.window
import org.w3c.dom.History

actual fun <C : Any> Router<C, *>.manageBrowserHistory(
    getInfo: (stack: List<C>) -> PageInfo
) {
    val history = BrowserHistory<C>()
    var oldStack = state.value.getStack()
    var skip = false

    state.subscribe { newState ->
        if (skip) {
            skip = false
            return@subscribe
        }

        val newStack = newState.getStack()
        val newInfo = getInfo(newStack)

        if (newStack.dropLast(1) == oldStack) {
            // TODO: push multiple
            history.push(newInfo, newState.activeChild.configuration)
        } else {
            history.replace(newInfo, newState.activeChild.configuration)
        }

        oldStack = newStack
    }

    // TODO: unsubscribe?
    window.onpopstate =
        {
            console.log(it.state)

            val newInfo: PageInfo? = it.state?.unsafeCast<PageInfo>()

            if (newInfo != null) {

            }

            val currentState = state.value
            val currentStack = currentState.getStack()


            if (currentStack.size < 2) {
                history.replace(getInfo(currentStack), currentState.activeChild.configuration)
            } else {
                val prevInfo = getInfo(currentStack.dropLast(1))
                if (window.history.getPageInfo()?.url == prevInfo.url) {
                    skip = true
                    pop()
                    history.pop()
                } else {
                    history.replace(getInfo(currentStack), currentState.activeChild.configuration)
                }
            }
        }
}

private class BrowserHistory<C : Any> {

    private val stack = ArrayList<HistoryEntry<C>>()
    private var index = -1

    fun push(pageInfo: PageInfo, configuration: C) {
        window.history.pushState(pageInfo)

        while (stack.lastIndex > index) {
            stack.removeLast()
        }
        stack += HistoryEntry(pageInfo, configuration)
        index++
    }

    fun replace(pageInfo: PageInfo, configuration: C) {
        check(index >= 0)
        window.history.replaceState(pageInfo)
        stack[stack.lastIndex] = HistoryEntry(pageInfo, configuration)
    }

    fun pop() {
        check(index > 0)
        index--
    }
}

private class HistoryEntry<out C : Any>(
    val pageInfo: PageInfo,
    val configuration: C,
)

private fun History.pushState(info: PageInfo) {
    console.log("pushState")
    console.log(info)
    pushState(data = info, title = info.title ?: "", url = info.url)
}

private fun History.replaceState(info: PageInfo) {
    console.log("replaceState")
    console.log(info)
    replaceState(data = info, title = info.title ?: "", url = info.url)
}

private fun History.getPageInfo(): PageInfo? =
    state?.unsafeCast<PageInfo>()

private fun <C : Any> RouterState<C, *>.getStack(): List<C> =
    backStack.map(Child<C, *>::configuration) + activeChild.configuration
