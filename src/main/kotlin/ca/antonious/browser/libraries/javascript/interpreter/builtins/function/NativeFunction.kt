package ca.antonious.browser.libraries.javascript.interpreter.builtins.function

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.lexer.SourceInfo

open class NativeFunction(
    functionPrototype: JavascriptObject = JavascriptObject(),
    private val body: (NativeExecutionContext) -> JavascriptValue
) : FunctionObject(functionPrototype) {

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

fun JavascriptObject.setNonEnumerableNativeFunction(name: String, body: (NativeExecutionContext) -> JavascriptValue) {
    setNonEnumerableProperty(name, JavascriptValue.Object(
        NativeFunction(JavascriptObject(), body)
    ))
}
