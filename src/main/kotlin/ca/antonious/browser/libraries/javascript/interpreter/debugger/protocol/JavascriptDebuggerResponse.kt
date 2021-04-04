package ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol

sealed class JavascriptDebuggerResponse {
    abstract val type: String

    data class BreakpointHit(
        val line: Int,
        val currentScopeVariables: Map<String, String>
    ) : JavascriptDebuggerResponse() {
        override val type = "breakpoint_hit"
    }

    data class EvaluationFinished(
        val result: String
    ) : JavascriptDebuggerResponse() {
        override val type = "eval_finished"
    }

    class Ack : JavascriptDebuggerResponse() {
        override val type = "ack"
    }
}
