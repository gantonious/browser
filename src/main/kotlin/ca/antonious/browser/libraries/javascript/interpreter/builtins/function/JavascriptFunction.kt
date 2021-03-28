package ca.antonious.browser.libraries.javascript.interpreter.builtins.function

import ca.antonious.browser.libraries.javascript.ast.JavascriptStatement
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptScope

open class JavascriptFunction(
    val name: String,
    val parameterNames: List<String>,
    val body: JavascriptStatement.Block,
    val parentScope: JavascriptScope
) : FunctionObject() {

    override fun call(nativeExecutionContext: NativeExecutionContext): JavascriptValue {
        return nativeExecutionContext.interpreter.interpretFunction(
            callLocation = nativeExecutionContext.callLocation,
            thisBinding = nativeExecutionContext.thisBinding,
            arguments = nativeExecutionContext.arguments,
            javascriptFunction = this
        )
    }

    override fun toString(): String {
        return "function (${parameterNames.joinToString(", ")}) {...}"
    }
}
