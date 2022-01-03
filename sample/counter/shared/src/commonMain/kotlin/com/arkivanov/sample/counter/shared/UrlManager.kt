package com.arkivanov.sample.counter.shared

import com.arkivanov.decompose.router.Router
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.pop

// TODO: better name?
expect fun <C : Any> Router<C, *>.manageBrowserHistory(
    getInfo: (stack: List<C>) -> PageInfo
)

data class PageInfo(
    val url: String,
    val title: String? = null,
)

//val window: Window = TODO()
//
//interface Window {
//    val history: History
//    var onpopstate: ((PopStateEvent) -> Unit)?
//}
//
//interface PopStateEvent
//
//interface History {
//    fun pushState(data: Any?, title: String, url: String?)
//    fun replaceState(data: Any?, title: String, url: String?)
//}
//
//actual fun <C : Any, T : Any> Router<C, T>.manageBrowserHistory(
//    getInfo: (RouterState<C, T>) -> PageInfo
//) {
//    var oldState = state.value
//
//    state.subscribe { newState ->
//        if (newState !== oldState) {
//            return@subscribe
//        }
//
//        val newInfo = getInfo(newState)
//
//        if (newState.backStack == oldState.backStack + oldState.activeChild) {
//            window.history.pushState(data = null, title = newInfo.title ?: "", url = newInfo.url)
//        } else {
//            window.history.replaceState(data = null, title = newInfo.title ?: "", url = newInfo.url)
//        }
//
//        oldState = newState
//    }
//
//    // TODO: unsubscribe?
//    window.onpopstate =
//        {
//            pop()
//        }
//}

//interface UrlManager {
//
//    fun push(url: String, title: String? = null)
//
//    fun replace(url: String, title: String? = null)
//}
//
//fun <C : Any, T : Any> UrlManager.attachToRouter(router: Router<C, T>, getInfo: (RouterState<C, T>) -> PageInfo) {
//    var oldState = router.state.value
//
//    router.state.subscribe { newState ->
//        if (newState !== oldState) {
//            return@subscribe
//        }
//
//        val newInfo = getInfo(newState)
//
//        if (newState.backStack == oldState.backStack + oldState.activeChild) {
//            push(url = newInfo.url, title = newInfo.title)
//        } else {
//            replace(url = newInfo.url, title = newInfo.title)
//        }
//
//        oldState = newState
//    }
//}
