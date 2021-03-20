package ca.antonious.browser.libraries.javascript.interpreter.builtins.`object`

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.number.NumberObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.string.StringObject
import ca.antonious.browser.libraries.javascript.interpreter.setNonEnumerableNativeFunction

object ObjectPrototype : JavascriptObject(prototype = null) {
    init {
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
