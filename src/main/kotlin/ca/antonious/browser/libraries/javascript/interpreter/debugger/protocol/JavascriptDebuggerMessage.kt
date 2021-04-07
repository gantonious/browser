package ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol

sealed class JavascriptDebuggerMessage {
    abstract val type: String

    data class BreakpointHit(
        val line: Int,
    ) : JavascriptDebuggerMessage() {
        override val type = "breakpoint_hit"
    }
}
