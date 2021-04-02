package ca.antonious.browser.libraries.javascript.interpreter.builtins.regex

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.setNonEnumerableNativeFunction

object RegExpPrototype : JavascriptObject() {
    init {
        setNonEnumerableNativeFunction("test") { executionContext ->
            val regexObject = executionContext.thisBinding as? RegExpObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val valueToTest = executionContext.arguments.first().toString()

            JavascriptValue.Boolean(Regex(regexObject.regex).matches(valueToTest))
        }
    }
}
