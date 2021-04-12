package ca.antonious.browser.libraries.javascript.interpreter.builtins.error

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

class ErrorPrototype(interpreter: JavascriptInterpreter) : JavascriptObject(interpreter.objectPrototype) {
    override fun initialize() {
        setNonEnumerableProperty("name", JavascriptValue.String("Error"))
        setNonEnumerableProperty("message", JavascriptValue.String(""))

        setNonEnumerableNativeFunction("toString") { nativeExecutionContext ->
            val errorName = nativeExecutionContext.thisBinding.getProperty("name")
            val message = nativeExecutionContext.thisBinding.getProperty("message")
            JavascriptValue.String("$errorName: $message")
        }
    }
}
