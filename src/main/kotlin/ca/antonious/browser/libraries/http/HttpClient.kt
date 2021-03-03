package ca.antonious.browser.libraries.http

import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class HttpClient {
    fun execute(httpRequest: HttpRequest): Task<HttpResponse> {
        val socket = Socket("localhost", 8080)
        val task = HttpTask { socket.close() }

        Thread {
            socket.use {
                OutputStreamWriter(socket.getOutputStream()).let {
                    val rawRequest = "${httpRequest.method.name.toUpperCase()} / HTTP/1.1\r\nHost: skrundz.ca\r\nConnection: close\r\n\r\n"
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