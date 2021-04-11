package ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol

sealed class JavascriptDebuggerMessage {
    abstract val type: String

    data class BreakpointHit(
        val line: Int,
    ) : JavascriptDebuggerMessage() {
        override val type = "breakpoint_hit"
    }

    data class UncaughtError(
        val error: String
    ) : JavascriptDebuggerMessage() {
        override val type = "uncaught_error"
    }

    data class SourceLoaded(
        val filename: String
    ) : JavascriptDebuggerMessage() {
        override val type = "source_loaded"
    }

}
