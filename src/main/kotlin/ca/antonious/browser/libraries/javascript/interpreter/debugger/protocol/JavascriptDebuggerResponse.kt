package ca.antonious.browser.libraries.javascript.interpreter.debugger.protocol

sealed class JavascriptDebuggerResponse {
    data class EvaluationFinished(
        val result: String,
        val expandPath: String?
    ) : JavascriptDebuggerResponse()

    data class GetStackResponse(
        val frames: List<StackFrameInfo>
    ) : JavascriptDebuggerResponse() {

        data class StackFrameInfo(
            val name: String,
            val line: Int,
            val column: Int,
            val filename: String
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

    data class GetSourceResponse(
        val source: String
    ) : JavascriptDebuggerResponse()

    data class GetStatusResponse(
        val status: Status
    ) : JavascriptDebuggerResponse() {
        enum class Status {
            Running,
            Paused
        }
    }
}
