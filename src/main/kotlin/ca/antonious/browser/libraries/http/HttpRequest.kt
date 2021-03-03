package ca.antonious.browser.libraries.http

data class HttpRequest(
    val url: String,
    val method: HttpMethod
)