package ca.antonious.browser.libraries.http

class HttpResponseParser {
    fun parse(response: String): HttpResponse {
        val responseLines = response.split("\n")
        val responseStatus = responseLines.first().split(" ")[1].toInt()
        val responseHeaders = responseLines.drop(1).takeWhile { it != "\r" }
        val responseBody = responseLines.dropWhile { it != "\r" }.drop(1)

        return HttpResponse(
            code = responseStatus,
            headers = responseHeaders.map {
                val splitByColon = it.split(":")
                splitByColon[0].trim() to splitByColon[1].trim()
            }.toMap(),
            body = responseBody.joinToString("\n")
        )
    }
}