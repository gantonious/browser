package ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol

sealed class JavascriptDebuggerRequest {

    data class SetBreakpoints(
        val breakpoints: List<BreakpointInfo>
    ) : JavascriptDebuggerRequest() {
        data class BreakpointInfo(
            val line: Int,
            val filename: String
        )
    }

    data class Evaluate(
        val javascript: String
    ) : JavascriptDebuggerRequest()

    data class GetVariables(
        val frameIndex: Int
    ) : JavascriptDebuggerRequest()
}
