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

    data class GetStackResponse(
        val frames: List<StackFrameInfo>
    ) : JavascriptDebuggerResponse() {
        override val type = "get_stack_response"

        data class StackFrameInfo(
            val name: String,
            val line: Int,
            val column: Int
        )
    }

    data class GetVariablesResponse(
        val variables: List<VariableInfo>
    ) : JavascriptDebuggerResponse() {
        override val type = "get_variables_response"

        data class VariableInfo(
            val name: String,
            val type: String,
            val value: String
        )
    }

    class Ack : JavascriptDebuggerResponse() {
        override val type = "ack"
    }
}
