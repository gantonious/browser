package ca.antonious.browser.libraries.javascript.interpreter.builtins.`object`

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.setNonEnumerableNativeFunction

object ObjectPrototype : JavascriptObject(prototype = null) {
    init {
        setNonEnumerableNativeFunction("valueOf") { nativeExecutionContext ->
            JavascriptValue.Object(nativeExecutionContext.thisBinding)
        }

        setNonEnumerableNativeFunction("toString") { nativeExecutionContext ->
            JavascriptValue.String(nativeExecutionContext.thisBinding.toString())
        }
    }
}