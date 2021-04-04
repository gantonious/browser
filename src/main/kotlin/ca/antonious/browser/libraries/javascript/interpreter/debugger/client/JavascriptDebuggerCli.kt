package ca.antonious.browser.libraries.javascript.interpreter.debugger.client

import ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol.JavascriptDebuggerResponse
import ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol.JavascriptDebuggerRequest
import kotlin.system.exitProcess

fun main() {
    val loadingIndicators = listOf('⠋', '⠙', '⠹', '⠸', '⠼', '⠴', '⠦', '⠧', '⠇', '⠏')
    var loadingIndex = 0
    var prompt = "Running"
    var nextMessage: ClientMessage? = null

    val debuggerClient = JavascriptDebuggerClient { clientMessage ->
        nextMessage = clientMessage
    }

    println("Connecting to debug server...")
    debuggerClient.connectBlocking()

    while (true) {
        print("\r[$prompt]: ")
        val nextLine = readLine() ?: ""

        when {
            nextLine.startsWith("sb") -> {
                val lineNumber = nextLine.split("sb ")[1].toInt() - 1
                debuggerClient.sendDebuggerRequest(JavascriptDebuggerRequest.SetBreakpoint(lineNumber))
            }
            nextLine.startsWith("r") -> {
                try {
                    val command = nextLine.split("r ")[1]
                    debuggerClient.sendDebuggerRequest(JavascriptDebuggerRequest.Execute(command))
                } catch (ex: Exception) {
                    continue
                }
            }
            nextLine == "c" -> {
                debuggerClient.sendDebuggerRequest(JavascriptDebuggerRequest.Continue())
                prompt = "Running"
            }
            nextLine == "e" -> break
            else -> continue
        }

        while (true) {
            if (nextMessage == null) {
                print("\r${loadingIndicators[loadingIndex]}")
                loadingIndex = (loadingIndex + 1) % loadingIndicators.size
                Thread.sleep(128)
            } else {
                break
            }
        }

        when (val currentMessage = nextMessage!!) {
            is ClientMessage.Closed -> {
                println("Debugger server disconnected")
                exitProcess(0)
            }
            is ClientMessage.ServerMessage -> {
                when (currentMessage.debuggerResponse) {
                    is JavascriptDebuggerResponse.BreakpointHit -> {
                        println("\rBreakpoint reached:")

                        println("Local variables: {")
                        currentMessage.debuggerResponse.currentScopeVariables.forEach { key, value ->
                            println("    $key: $value")
                        }
                        println("}")

                        prompt = "Paused:${currentMessage.debuggerResponse.line + 1}"
                    }
                    is JavascriptDebuggerResponse.EvaluationFinished -> {
                        println("\r${currentMessage.debuggerResponse.result}")
                    }
                    is JavascriptDebuggerResponse.Ack -> Unit
                }
            }
        }

        nextMessage = null
    }

    debuggerClient.close()
}
