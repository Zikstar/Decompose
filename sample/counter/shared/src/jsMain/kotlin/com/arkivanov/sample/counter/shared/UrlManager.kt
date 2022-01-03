package com.arkivanov.sample.counter.shared

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.Router
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.pop
import com.arkivanov.decompose.router.push
import kotlinx.browser.window
import org.w3c.dom.History

// TODO: use RouterState instead of List<C>?
actual fun <C : Any> Router<C, *>.manageBrowserHistory(
    getInfo: (stack: List<C>) -> PageInfo
) {
    var oldStack = state.value.getStack()
    var isObserverEnabled = true

    state.subscribe { newState ->
        val newStack = newState.getStack()

        if (!isObserverEnabled) {
            oldStack = newStack
            return@subscribe
        }


        val newConfiguration = newState.activeChild.configuration
        val newInfo = getInfo(newStack) // TODO: Not always needed

        when {
            newStack == oldStack -> window.history.replaceState(newInfo, newConfiguration)

            newStack.dropLast(1) == oldStack.dropLast(1) -> {
                window.history.replaceState(newInfo, newConfiguration)
            }

            newStack.dropLast(1) == oldStack -> {
                // TODO: pop multiple
                window.history.pushState(newInfo, newConfiguration)
            }

            newStack == oldStack.dropLast(1) -> {
                // TODO: push multiple
                window.history.back()
            }

            else -> error("Unsupported, newStack: $newStack, oldStack: $oldStack")
        }

        oldStack = newStack
    }

    val poppedConfigurations = ArrayList<C>()

    window.onpopstate = { event ->
        val newConfigurationKey: Int? = event.state?.unsafeCast<Int>()

        if (newConfigurationKey != null) {
            val currentState = state.value

            if (newConfigurationKey == currentState.activeChild.configuration.hashCode()) {
                // TODO: remove?
                // no-op
            } else if (newConfigurationKey == currentState.backStack.lastOrNull()?.configuration?.hashCode()) {
                poppedConfigurations += currentState.activeChild.configuration
                isObserverEnabled = false
                pop()
                isObserverEnabled = true
            } else if (newConfigurationKey == poppedConfigurations.lastOrNull()?.hashCode()) {
                isObserverEnabled = false
                push(poppedConfigurations.removeLast())
                isObserverEnabled = true
            } else {
                error("Unsupported")
            }
        }
    }
}

private fun History.pushState(pageInfo: PageInfo, configuration: Any) {
    pushState(data = configuration.hashCode(), title = pageInfo.title ?: "", url = pageInfo.url)
}

private fun History.replaceState(pageInfo: PageInfo, configuration: Any) {
    replaceState(data = configuration.hashCode(), title = pageInfo.title ?: "", url = pageInfo.url)
}

//private class BrowserHistory<C : Any> {
//
//    private val _stack = ArrayList<>()
//}

//// TODO: test external links and going back
//actual fun <C : Any> Router<C, *>.manageBrowserHistory(
//    getInfo: (stack: List<C>) -> PageInfo
//) {
//    // TODO: Avoid initial entry?
//    val history = BrowserHistory(HistoryEntry(getInfo(state.value.getStack()), state.value.activeChild.configuration))
//    var oldStack = state.value.getStack()
//    var skip = true
//
//    state.subscribe { newState ->
//        if (skip) {
//            skip = false
//            return@subscribe
//        }
//
//        val newStack = newState.getStack()
//        val newInfo = getInfo(newStack)
//        val newEntry = HistoryEntry(newInfo, newState.activeChild.configuration)
//
//        if (newStack.dropLast(1) == oldStack) {
//            // TODO: push multiple
//            history.push(newEntry)
//        } else if (newStack == oldStack.dropLast(1)) {
//            // TODO: pop multiple
////            history.goBack()
//            window.history.back()
//        } else {
//            // TODO: Handle?
//            error("Unsupported, newStack: $newStack, oldStack: $oldStack")
//        }
//
//        console.log(history)
//
//        oldStack = newStack
//    }
//
//    // TODO: unsubscribe?
//    window.onpopstate =
//        { event ->
//            console.log(event.state)
//
//            val newEntry: HistoryEntry<C>? = event.state?.unsafeCast<HistoryEntry<C>>()
//
//            if (newEntry != null) {
//                val newIndex = history.stack.indexOfFirst { it.key == newEntry.key }
//
//                if (newIndex == history.index + 1) {
//                    console.log("Forward clicked")
//                    history.forward()
//                    skip = true
//                    push(history.stack[history.index].configuration)
//                } else if (newIndex == history.index - 1) {
//                    val currentState = state.value
//                    console.log("Back clicked")
//                    history.back()
//
//                    if (history.stack[history.index].configuration == currentState.backStack.lastOrNull()?.configuration) {
//                        skip = true
//                        pop()
//                    } else {
////                        history.replace(HistoryEntry(getInfo(currentState.getStack()), currentState.activeChild.configuration))
//                        // TODO: Prevent going forward?
//                    }
//                } else {
//                    error("Illegal new history index: $newIndex. History: ${history.index}, ${history.stack}.")
//                }
//
//                console.log(history)
//            }
//        }
//}
//
//private class BrowserHistory<C : Any>(initialEntry: HistoryEntry<C>) {
//
//    private val _stack = ArrayList<HistoryEntry<C>>()
//    val stack: List<HistoryEntry<C>> = _stack
//
//    var index: Int = 0
//        private set
//
//    init {
//        _stack += initialEntry
//        window.history.replaceState(initialEntry)
//    }
//
//    fun push(entry: HistoryEntry<C>) {
//        window.history.pushState(entry)
//
//        while (_stack.lastIndex > index) {
//            _stack.removeLast()
//        }
//        _stack += entry
//        index++
//    }
//
//    fun replace(entry: HistoryEntry<C>) {
//        check(index >= 0)
//        window.history.replaceState(entry)
//        _stack[index] = entry
//    }
//
//    fun forward() {
//        check(index < _stack.lastIndex)
//        index++
//    }
//
////    fun goBack() {
////        back()
////        window.history.back()
////    }
//
//    fun back() {
//        check(index > 0)
//        index--
//    }
//
//    override fun toString(): String =
//        "BrowserHistory {index: $index, stack: $_stack}"
//}

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
