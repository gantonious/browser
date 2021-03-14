package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

open class NativeFunction(
    val functionPrototype: JavascriptObject = JavascriptObject(),
    val body: (NativeExecutionContext) -> JavascriptValue
) : JavascriptObject() {

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
    setNonEnumerableProperty(name, JavascriptValue.Object(NativeFunction(JavascriptObject(), body)))
}