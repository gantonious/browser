package ca.antonious.browser.libraries.http

data class HttpResponse(
    val code: Int,
    val headers: Map<String, String>,
    val body: String
)
