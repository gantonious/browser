package ca.antonious.browser.libraries.http

interface Task<T> {
    fun onSuccess(block: (T) -> Unit)
    fun cancel()
}
