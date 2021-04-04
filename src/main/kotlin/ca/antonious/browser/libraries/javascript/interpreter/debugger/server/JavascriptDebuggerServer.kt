package ca.antonious.browser.libraries.javascript.interpreter.debugger.server

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol.JavascriptDebuggerResponse
import ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol.JavascriptDebuggerRequest
import ca.antonious.browser.libraries.javascript.interpreter.debugger.utils.SubclassDeserializer
import ca.antonious.browser.libraries.javascript.lexer.SourceInfo
import com.google.gson.GsonBuilder
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock

class JavascriptDebuggerServer(
    private val interpreter: JavascriptInterpreter
) : WebSocketServer(InetSocketAddress(31256)) {

    val debuggerLock = ReentrantLock()
    private val debuggerExecutor = Executors.newSingleThreadExecutor()

    private val breakpoints = mutableSetOf<Int>()

    private val gson = GsonBuilder()
        .registerTypeAdapter(
            JavascriptDebuggerRequest::class.java,
            SubclassDeserializer(
                typeFieldName = "type",
                classMap = mapOf(
                    "set_breakpoint" to JavascriptDebuggerRequest.SetBreakpoint::class.java,
                    "continue" to JavascriptDebuggerRequest.Continue::class.java,
                    "execute" to JavascriptDebuggerRequest.Execute::class.java
                )
            )
        )
        .create()

    private var activeConnection: WebSocket? = null

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        println("DebugServer: Client connected")
        activeConnection = conn
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        println("DebugServer: Client disconnected")
        activeConnection = null
        debuggerExecutor.submit { debuggerLock.unlock() }
    }

    override fun onMessage(conn: WebSocket, message: String) {
        try {
            when (val debuggerRequest = gson.fromJson(message, JavascriptDebuggerRequest::class.java)) {
                is JavascriptDebuggerRequest.SetBreakpoint -> {
                    breakpoints.add(debuggerRequest.line)
                    sendMessage(JavascriptDebuggerResponse.Ack())
                }
                is JavascriptDebuggerRequest.Continue ->  {
                    debuggerExecutor.submit { debuggerLock.unlock() }
                    sendMessage(JavascriptDebuggerResponse.Ack())
                }
                is JavascriptDebuggerRequest.Execute -> {
                    debuggerExecutor.submit {
                        try {
                            val value = interpreter.interpret(debuggerRequest.command)
                            sendMessage(JavascriptDebuggerResponse.EvaluationFinished(value.toString()))
                        } catch (ex: Exception) {
                            sendMessage(JavascriptDebuggerResponse.EvaluationFinished(ex.message ?: "Uncaught error"))
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            sendMessage(JavascriptDebuggerResponse.EvaluationFinished(ex.message ?: "Uncaught error"))
        }
    }

    fun onSourceInfoUpdated(sourceInfo: SourceInfo) {
        if (sourceInfo.line in breakpoints) {
            debuggerExecutor.submit { debuggerLock.lock() }.get()
            sendMessage(
                JavascriptDebuggerResponse.BreakpointHit(
                    line = sourceInfo.line,
                    currentScopeVariables = interpreter.stack.peek().scope.variables.map { it.key to it.value.toString() }.toMap()
                )
            )
        }
    }

    private fun sendMessage(message: JavascriptDebuggerResponse) {
        activeConnection?.send(gson.toJson(message))
    }

    override fun onStart() = Unit
    override fun onError(conn: WebSocket, ex: Exception) = Unit
}
