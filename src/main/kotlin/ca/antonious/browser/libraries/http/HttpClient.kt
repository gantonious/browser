package ca.antonious.browser.libraries.http

import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.util.concurrent.Executors
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class HttpClient {
    private val mainThreadExecutor = Executors.newSingleThreadExecutor()

    fun execute(httpRequest: HttpRequest): Task<HttpResponse> {
        val requestUri = httpRequest.url

        val socket = if (requestUri.port == 443) {
            SSLSocketFactory.getDefault().createSocket(requestUri.host, requestUri.port)
        } else {
            Socket(requestUri.host, requestUri.port)
        }

        val task = HttpTask { socket.close() }

        Thread {
            socket.use {
                (it as? SSLSocket)?.startHandshake()
                OutputStreamWriter(socket.getOutputStream()).let {
                    val rawRequest = "${httpRequest.method.name.toUpperCase()} ${requestUri.path} HTTP/1.1\r\nHost: ${requestUri.host}\r\nConnection: close\r\n\r\n"
                    it.write(rawRequest)
                    it.flush()
                }

                InputStreamReader(socket.getInputStream()).let {
                    val response = HttpResponseParser().parse(it.readText())
                    mainThreadExecutor.execute {
                        task.invokeSuccess(response)
                    }
                }
            }
        }.start()

        return task
    }
}