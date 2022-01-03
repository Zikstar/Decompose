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
            window.history.pushState(newInfo)
        } else {
            window.history.replaceState(newInfo)
        }

        oldStack = newStack
    }

    // TODO: unsubscribe?
    window.onpopstate =
        {
            console.log(it)
            val currentStack = state.value.getStack()
            if (currentStack.size < 2) {
                window.history.replaceState(getInfo(currentStack))
            } else {
                val prevInfo = getInfo(currentStack.dropLast(1))
                if (window.history.getPageInfo()?.url == prevInfo.url) {
                    skip = true
                    pop()
                } else {
                    window.history.replaceState(getInfo(currentStack))
                }
            }
        }
}

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
