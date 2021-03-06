package ca.antonious.browser.libraries.javascript.interpreter.builtins.function

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

abstract class FunctionObject(
    interpreter: JavascriptInterpreter,
    functionPrototype: JavascriptObject,
) : JavascriptObject(prototype = interpreter.functionPrototype) {

    open val name: String = "anonymous"

    open var boundThis: JavascriptObject? = null

    val functionPrototype: JavascriptObject
        get() {
            return getProperty("prototype").valueAs<JavascriptValue.Object>()?.value ?: interpreter.makeObject()
        }

    init {
        setNonEnumerableProperty("prototype", JavascriptValue.Object(functionPrototype))
    }

    abstract fun call(nativeExecutionContext: NativeExecutionContext): JavascriptValue
}
