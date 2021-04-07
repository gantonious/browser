package ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol

sealed class JavascriptDebuggerResponse {
    data class EvaluationFinished(
        val result: String
    ) : JavascriptDebuggerResponse()

    data class GetStackResponse(
        val frames: List<StackFrameInfo>
    ) : JavascriptDebuggerResponse() {

        data class StackFrameInfo(
            val name: String,
            val line: Int,
            val column: Int
        )
    }

    data class GetVariablesResponse(
        val variables: List<VariableInfo>
    ) : JavascriptDebuggerResponse() {

        data class VariableInfo(
            val name: String,
            val type: String,
            val value: String,
            val expandPath: String?
        )
    }
}
