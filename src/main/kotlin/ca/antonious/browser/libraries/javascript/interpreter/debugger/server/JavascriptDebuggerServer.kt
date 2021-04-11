package ca.antonious.browser.libraries.javascript.interpreter.debugger.server

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptScope
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptStackFrame
import ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol.JavascriptDebuggerMessage
import ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol.JavascriptDebuggerRequest
import ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol.JavascriptDebuggerResponse
import ca.antonious.browser.libraries.javascript.lexer.SourceInfo
import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.send
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Stack
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock

class JavascriptDebuggerServer(
    private val interpreter: JavascriptInterpreter
) {
    private val ignoredFiles = setOf("unknown", "evaluate")

    private val executionLock = ReentrantLock()
    private val debuggerExecutor = Executors.newSingleThreadExecutor()
    private val webSockets = mutableSetOf<WebSocketSession>()

    private var breakOnNextStatement = false
    private val breakpoints = mutableSetOf<Int>()
    private val filenameBreakpoints = mutableSetOf("jquery.js")

    private val gson = Gson()
    private var error: JavascriptInterpreter.ControlFlowInterruption.Error? = null
    private var evaluatedObjects = mutableMapOf<String, JavascriptObject>()
    private val loadedSources = mutableMapOf<String, String>()

    private val stack: Stack<JavascriptStackFrame>
        get() {
            val capturedError = error

            return if (capturedError != null) {
                Stack<JavascriptStackFrame>().apply {
                    addAll(capturedError.trace.reversed())
                }
            } else {
                interpreter.stack
            }
        }

    init {
        embeddedServer(Netty, port = 31256) {
            install(WebSockets)
            install(ContentNegotiation) {
                gson()
            }

            routing {
                route("status") {
                    get {
                        val isPaused = withContext(debuggerExecutor.asCoroutineDispatcher()) {
                            executionLock.isLocked
                        }

                        call.respond(
                            JavascriptDebuggerResponse.GetStatusResponse(
                                status = if (isPaused) {
                                    JavascriptDebuggerResponse.GetStatusResponse.Status.Paused
                                } else {
                                    JavascriptDebuggerResponse.GetStatusResponse.Status.Running
                                }
                            )
                        )
                    }
                }

                route("sources") {
                    get {
                        call.respond(JavascriptDebuggerResponse.GetSourcesResponse(sourceNames = loadedSources.keys.toList()))
                    }
                }

                route("source") {
                    get("{filename}") {
                        val source = loadedSources[call.parameters["filename"]]

                        if (source == null) {
                            call.respond(HttpStatusCode.NotFound)
                        } else {
                            call.respond(JavascriptDebuggerResponse.GetSourceResponse(source))
                        }
                    }
                }

                route("stack") {
                    get {
                        val frames = stack.map { it.copy() }.map {
                            JavascriptDebuggerResponse.GetStackResponse.StackFrameInfo(
                                name = it.name,
                                line = it.sourceInfo.line,
                                column = it.sourceInfo.column,
                                filename = it.sourceInfo.filename
                            )
                        }

                        call.respond(JavascriptDebuggerResponse.GetStackResponse(frames))
                    }
                }

                route("variables/{...}") {
                    get {
                        val path = call.request.path().trimEnd('/')
                        val pathParts = path.split("/").drop(2)
                        val parentPath = pathParts.joinToString("/")
                        val firstPathPart = pathParts.firstOrNull()

                        when {
                            firstPathPart == null -> call.respond(HttpStatusCode.NotFound)
                            firstPathPart.toIntOrNull() != null -> {
                                val stackIndex = firstPathPart.toInt()
                                if (stackIndex < 0 || stackIndex >= stack.size) {
                                    call.respond(HttpStatusCode.NotFound)
                                    return@get
                                }

                                val scope = stack[stackIndex].scope

                                val variableName = pathParts.getOrNull(1)
                                if (variableName == null) {
                                    call.respond(scope.toVariablesResponse(parentPath))
                                    return@get
                                }

                                var currentObject = scope
                                    .getVariable(variableName).value
                                    .valueAs<JavascriptValue.Object>()?.value

                                if (currentObject == null) {
                                    call.respond(HttpStatusCode.NotFound)
                                    return@get
                                }

                                pathParts.drop(2).forEach { pathPart ->
                                    currentObject = currentObject?.getProperty(pathPart)?.valueAs<JavascriptValue.Object>()?.value

                                    if (currentObject == null) {
                                        call.respond(HttpStatusCode.NotFound)
                                        return@get
                                    }
                                }

                                call.respond(currentObject!!.toVariablesResponse(parentPath))
                            }
                            else -> {
                                var currentObject = if (firstPathPart in evaluatedObjects) {
                                    evaluatedObjects[firstPathPart]
                                } else {
                                    interpreter.stack.peek().scope
                                        .getVariable(firstPathPart).value
                                        .valueAs<JavascriptValue.Object>()?.value
                                }

                                if (currentObject == null) {
                                    call.respond(HttpStatusCode.NotFound)
                                    return@get
                                }

                                pathParts.drop(1).forEach { pathPart ->
                                    currentObject = currentObject?.getProperty(pathPart)?.valueAs<JavascriptValue.Object>()?.value

                                    if (currentObject == null) {
                                        call.respond(HttpStatusCode.NotFound)
                                        return@get
                                    }
                                }

                                call.respond(currentObject!!.toVariablesResponse(parentPath))
                            }
                        }
                    }
                }

                route("breakpoints") {
                    post {
                        val body = call.receive<JavascriptDebuggerRequest.SetBreakpoints>()
                        breakpoints.clear()
                        breakpoints.addAll(body.breakpoints.map { it.line })
                    }
                }

                route("continue") {
                    post {
                        withContext(debuggerExecutor.asCoroutineDispatcher()) {
                            error = null
                            evaluatedObjects.clear()
                            executionLock.unlock()
                        }

                        call.respond(HttpStatusCode.OK)
                    }
                }

                route("stepOver") {
                    post {
                        breakOnNextStatement = true
                        withContext(debuggerExecutor.asCoroutineDispatcher()) {
                            executionLock.unlock()
                        }

                        call.respond(HttpStatusCode.OK)
                    }
                }

                route("evaluate") {
                    post {
                        try {
                            val body = call.receive<JavascriptDebuggerRequest.Evaluate>()
                            val evaluationStack = stack.take(body.frameIndex + 1)
                            val value = withContext(debuggerExecutor.asCoroutineDispatcher()) {
                                interpreter.interpretWithExplicitStack(
                                    javascript = body.javascript,
                                    filename = "evaluate",
                                    stack = Stack<JavascriptStackFrame>().apply {
                                        addAll(evaluationStack)
                                    }
                                )
                            }

                            call.respond(
                                JavascriptDebuggerResponse.EvaluationFinished(
                                    result = value.toString(),
                                    expandPath = if (value is JavascriptValue.Object && value.value.allPropertyKeys.isNotEmpty()) {
                                        val evaluationId = UUID.randomUUID().toString()
                                        evaluatedObjects[evaluationId] = value.value
                                        evaluationId
                                    } else {
                                        null
                                    }
                                )
                            )
                        } catch (ex: Exception) {
                            call.respond((JavascriptDebuggerResponse.EvaluationFinished(ex.message ?: "Uncaught error", null)))
                        }
                    }
                }

                webSocket("events") {
                    println("DebugServer: Client connected")
                    webSockets.add(this)
                    for (frame in incoming) {}
                    webSockets.remove(this)
                    debuggerExecutor.submit { executionLock.unlock() }
                    breakpoints.clear()
                    evaluatedObjects.clear()
                    error = null
                    println("DebugServer: Client disconnected")
                }
            }
        }.start()
    }

    fun updateSourceLocation(sourceInfo: SourceInfo) {
        if (sourceInfo.filename !in loadedSources && sourceInfo.filename !in ignoredFiles) {
            loadedSources[sourceInfo.filename] = sourceInfo.source
            sendMessage(JavascriptDebuggerMessage.SourceLoaded(filename = sourceInfo.filename))
        }

        if (breakOnNextStatement || sourceInfo.line in breakpoints || sourceInfo.filename in filenameBreakpoints) {
            filenameBreakpoints.remove(sourceInfo.filename)
            breakOnNextStatement = false
            debuggerExecutor.submit { executionLock.lock() }.get()
            sendMessage(
                JavascriptDebuggerMessage.BreakpointHit(
                    line = sourceInfo.line
                )
            )
        }
    }

    fun pauseOnError(error: JavascriptInterpreter.ControlFlowInterruption.Error) {
        if (webSockets.isEmpty()) return
        if (executionLock.isLocked) return

        this.error = error
        debuggerExecutor.submit { executionLock.lock() }.get()
        sendMessage(JavascriptDebuggerMessage.UncaughtError(error = error.value.toString()))
    }

    fun awaitForContinue() {
        executionLock.lock()
        executionLock.unlock()
    }

    private fun sendMessage(message: JavascriptDebuggerMessage) {
        val rawMessage = gson.toJson(message)
        webSockets.forEach { GlobalScope.launch { it.send(rawMessage) } }
    }

    private fun JavascriptObject.toVariablesResponse(parentPath: String): JavascriptDebuggerResponse.GetVariablesResponse {
        return JavascriptDebuggerResponse.GetVariablesResponse(
            variables = allProperties.map { it.toVariableInfo(parentPath) }
        )
    }

    private fun JavascriptScope.toVariablesResponse(parentPath: String): JavascriptDebuggerResponse.GetVariablesResponse {
        return JavascriptDebuggerResponse.GetVariablesResponse(
            variables = (
                variables.map { (it.key to it.value).toVariableInfo(parentPath) } + if (parentScope != null && parentScope.type !is JavascriptScope.Type.Function) {
                    parentScope.toVariablesResponse(parentPath).variables
                } else {
                    emptyList()
                }
                ).distinctBy { it.name }
        )
    }

    private fun Pair<String, JavascriptValue>.toVariableInfo(parentPath: String): JavascriptDebuggerResponse.GetVariablesResponse.VariableInfo {
        val value = second
        return JavascriptDebuggerResponse.GetVariablesResponse.VariableInfo(
            name = first,
            value = if (second is JavascriptValue.String) {
                "\"$second\""
            } else {
                second.toString()
            },
            type = second.typeName,
            expandPath = if (value is JavascriptValue.Object && value.value.allPropertyKeys.isNotEmpty()) {
                "$parentPath/$first"
            } else {
                null
            }
        )
    }
}
