package com.arkivanov.sample.counter.shared

import com.arkivanov.decompose.router.Router

actual fun <C : Any> Router<C, *>.manageBrowserHistory(
    getInfo: (stack: List<C>) -> PageInfo
) {
    // no-op
}
