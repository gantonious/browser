package ca.antonious.browser.libraries.javascript.interpreter.builtins.number

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.setNonEnumerableNativeFunction

object NumberPrototype : JavascriptObject() {
    init {
        setNonEnumerableNativeFunction("valueOf") { executionContext ->
            JavascriptValue.Number((executionContext.thisBinding as NumberObject).value)
        }
    }
}