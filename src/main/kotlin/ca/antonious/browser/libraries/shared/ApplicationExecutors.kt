package ca.antonious.browser.libraries.shared

import java.util.concurrent.Executor

object ApplicationExecutors {
    var mainThreadExecutor: Executor? = null

    fun mainThreadExecutor(): Executor {
        return mainThreadExecutor ?: error("No main thread executor set. Is an application running?")
    }
}
