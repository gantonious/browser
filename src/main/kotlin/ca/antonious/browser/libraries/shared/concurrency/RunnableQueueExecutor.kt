package ca.antonious.browser.libraries.shared.concurrency

import java.util.concurrent.Executor
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class RunnableQueueExecutor : Executor {

    private val lock = ReentrantLock()
    private val queue = mutableListOf<Runnable>()

    override fun execute(command: Runnable) {
        lock.withLock {
            queue.add(command)
        }
    }

    fun runQueue() {
        lock.withLock {
            for (runnable in queue) {
                runnable.run()
            }
            queue.clear()
        }
    }
}
