package ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol

sealed class JavascriptDebuggerRequest {
    abstract val type: String

    data class SetBreakpoint(
        val line: Int
    ) : JavascriptDebuggerRequest() {
        override val type = "set_breakpoint"
    }

    data class SetBreakpoints(
        val breakpoints: List<BreakpointInfo>
    ) : JavascriptDebuggerRequest() {
        override val type = "set_breakpoints"

        data class BreakpointInfo(
            val line: Int,
            val filename: String
        )
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

    class GetVariables : JavascriptDebuggerRequest() {
        override val type = "get_variables"
    }
}
