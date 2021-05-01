package ca.antonious.browser.libraries.javascript.interpreter.builtins.function

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.shared.parsing.SourceInfo

open class NativeFunction(
    interpreter: JavascriptInterpreter,
    functionPrototype: JavascriptObject = interpreter.makeObject(),
    private val body: (NativeExecutionContext) -> JavascriptValue
) : FunctionObject(interpreter, functionPrototype) {

    override fun call(nativeExecutionContext: NativeExecutionContext): JavascriptValue {
        return body.invoke(nativeExecutionContext)
    }

    override fun toString(): String {
        return "function () { [native code] }"
    }
}

data class NativeExecutionContext(
    val callLocation: SourceInfo,
    val arguments: List<JavascriptValue>,
    val thisBinding: JavascriptObject,
    val interpreter: JavascriptInterpreter
)
