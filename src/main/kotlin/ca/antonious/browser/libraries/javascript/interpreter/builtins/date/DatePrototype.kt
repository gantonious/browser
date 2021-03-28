package ca.antonious.browser.libraries.javascript.interpreter.builtins.date

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.setNonEnumerableNativeFunction

object DatePrototype : JavascriptObject() {
    init {
        setNonEnumerableNativeFunction("valueOf") { nativeExecutionContext ->
            val dateObject = nativeExecutionContext.thisBinding as? DateObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            JavascriptValue.Number(dateObject.date.time.toDouble())
        }
    }
}
