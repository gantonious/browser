package ca.antonious.browser.libraries.http

class HttpTask(private val onCancel: () -> Unit) : Task<HttpResponse> {
    private var successListener: ((HttpResponse) -> Unit)? = null

    override fun onSuccess(block: (HttpResponse) -> Unit) {
        successListener = block
    }

    override fun cancel() {
        onCancel.invoke()
    }

    fun invokeSuccess(httpResponse: HttpResponse) {
        successListener?.invoke(httpResponse)
    }
}
