package ca.antonious.browser.libraries.http

data class HttpRequest(
    val url: Uri,
    val method: HttpMethod
)