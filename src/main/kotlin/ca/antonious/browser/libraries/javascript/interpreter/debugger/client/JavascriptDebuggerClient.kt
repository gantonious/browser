package ca.antonious.browser.libraries.javascript.interpreter.debugger.client

import ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol.JavascriptDebuggerResponse
import ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol.JavascriptDebuggerRequest
import ca.antonious.browser.libraries.javascript.interpreter.debugger.utils.SubclassDeserializer
import com.google.gson.GsonBuilder
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

class JavascriptDebuggerClient(
    private val listener: (ClientMessage) -> Unit
) : WebSocketClient(URI.create("ws://localhost:31256")) {

    private val gson = GsonBuilder()
        .registerTypeAdapter(
            JavascriptDebuggerResponse::class.java,
            SubclassDeserializer(
                typeFieldName = "type",
                classMap = mapOf(
                    "breakpoint_hit" to JavascriptDebuggerResponse.BreakpointHit::class.java,
                    "eval_finished" to JavascriptDebuggerResponse.EvaluationFinished::class.java,
                    "ack" to JavascriptDebuggerResponse.Ack::class.java
                )
            )
        )
        .create()

    override fun onOpen(handshakedata: ServerHandshake) = Unit
    override fun onError(ex: Exception?) = Unit

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        listener.invoke(ClientMessage.Closed)
    }

    override fun onMessage(message: String) {
        try {
            val debuggerMessage = gson.fromJson(message, JavascriptDebuggerResponse::class.java)
            listener.invoke(ClientMessage.ServerMessage(debuggerMessage))
        } catch (ex: Exception) {
            println("DebugClient: Couldn't consume message: ${message}.")
        }
    }

    fun sendDebuggerRequest(debuggerRequest: JavascriptDebuggerRequest) {
        send(gson.toJson(debuggerRequest))
    }
}

sealed class ClientMessage {
    data class ServerMessage(val debuggerResponse: JavascriptDebuggerResponse) : ClientMessage()
    object Closed : ClientMessage()
}
