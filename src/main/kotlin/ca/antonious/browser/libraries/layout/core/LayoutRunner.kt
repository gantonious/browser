package ca.antonious.browser.libraries.layout.core

import java.util.concurrent.Executor

interface LayoutRunner {
    val mainThreadExecutor: Executor
    fun runLayout(layoutNode: LayoutNode)
}
