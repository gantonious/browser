package ca.antonious.browser.libraries.http

data class Uri(
    val scheme: String,
    val host: String,
    val port: Int,
    val path: String
) {

    fun uriForPath(path: String): Uri {
        return when {
            path.startsWith("http") -> path.toUri()
            path.endsWith("/") -> {
                copy(path = "${this.path.removeSuffix("/")}/${path.removePrefix("/")}")
            }
            else -> {
                copy(path = "${this.path}/${path.removePrefix("/")}")
            }
        }
    }

    override fun toString(): String {
        return "$scheme://$host:$port/$path"
    }
}

fun String.toUri(): Uri {
    val scheme = split("://").let {
        if (it.count() == 1) {
            "http"
        } else {
            it.first()
        }
    }
    val hostAndPortAndPath = replace("$scheme://", "").split("/")
    val hostAndPort = hostAndPortAndPath.first().split(":")

    return Uri(
        scheme = scheme,
        host = hostAndPort.first(),
        port = hostAndPort.getOrNull(1)?.toInt() ?: when (scheme) {
            "https" -> 443
            else -> 80
        },
        path = "/${hostAndPortAndPath.drop(1).joinToString("/")}"
    )
}
