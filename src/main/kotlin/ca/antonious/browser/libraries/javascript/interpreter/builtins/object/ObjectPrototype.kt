package ca.antonious.browser.libraries.javascript.interpreter.builtins.`object`

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.number.NumberObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.string.StringObject

class ObjectPrototype(interpreter: JavascriptInterpreter) : JavascriptObject(interpreter = interpreter, prototype = null) {
    override fun initialize() {
        setNonEnumerableNativeFunction("valueOf") { nativeExecutionContext ->
            when (val thisBinding = nativeExecutionContext.thisBinding) {
                is StringObject -> JavascriptValue.String(thisBinding.value)
                is NumberObject -> JavascriptValue.Number(thisBinding.value)
                else -> JavascriptValue.Object(nativeExecutionContext.thisBinding)
            }
        }

        setNonEnumerableNativeFunction("toString") { nativeExecutionContext ->
            JavascriptValue.String(nativeExecutionContext.thisBinding.toString())
        }
    }
}
