package ca.antonious.browser.libraries.javascript.interpreter.builtins.function

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

abstract class FunctionObject(
    functionPrototype: JavascriptObject = JavascriptObject()
) : JavascriptObject(prototype = FunctionPrototype) {

    val functionPrototype: JavascriptObject
        get() {
            return getProperty("prototype").valueAs<JavascriptValue.Object>()?.value ?: JavascriptObject()
        }

    init {
        setNonEnumerableProperty("prototype", JavascriptValue.Object(functionPrototype))
    }

    abstract fun call(nativeExecutionContext: NativeExecutionContext): JavascriptValue
}
