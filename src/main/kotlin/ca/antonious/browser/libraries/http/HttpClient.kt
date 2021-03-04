package ca.antonious.browser.libraries.http

import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class HttpClient {
    fun execute(httpRequest: HttpRequest): Task<HttpResponse> {
        val requestUri = httpRequest.url.toUri()
        val socket = SSLSocketFactory.getDefault().createSocket(requestUri.host, requestUri.port) as SSLSocket
        val task = HttpTask { socket.close() }

        Thread {
            socket.use {
                it.startHandshake()
                OutputStreamWriter(socket.getOutputStream()).let {
                    val rawRequest = "${httpRequest.method.name.toUpperCase()} ${requestUri.path} HTTP/1.1\r\nHost: skrundz.ca\r\nConnection: close\r\n\r\n"
                    it.write(rawRequest)
                    it.flush()
                }

                InputStreamReader(socket.getInputStream()).let {
                    task.invokeSuccess(HttpResponseParser().parse(it.readText()))
                }
            }
        }.start()

        return task
    }
}