package ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol

sealed class JavascriptDebuggerRequest {
    abstract val type: String

    data class SetBreakpoint(
        val line: Int
    ) : JavascriptDebuggerRequest() {
        override val type = "set_breakpoint"
    }

    data class Execute(
        val command: String
    ) : JavascriptDebuggerRequest() {
        override val type = "execute"
    }

    class Continue : JavascriptDebuggerRequest() {
        override val type = "continue"
    }

    class GetStack : JavascriptDebuggerRequest() {
        override val type = "get_stack"
    }
}
