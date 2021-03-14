package ca.antonious.browser.libraries.javascript.interpreter.builtins

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

class NativeFunction(val body: (NativeExecutionContext) -> JavascriptValue) : JavascriptObject() {
    val functionPrototype = JavascriptObject()

    init {
        setNonEnumerableProperty("prototype", JavascriptValue.Object(functionPrototype))
    }

    override fun toString(): String {
        return "function () { [native code] }"
    }
}

data class NativeExecutionContext(
    val arguments: List<JavascriptValue>,
    val thisBinding: JavascriptObject,
    val interpreter: JavascriptInterpreter
)

fun JavascriptObject.setNonEnumerableNativeFunction(name: String, body: (NativeExecutionContext) -> JavascriptValue) {
    setNonEnumerableProperty(name, JavascriptValue.Object(NativeFunction(body)))
}