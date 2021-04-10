package ca.antonious.browser.libraries.javascript.interpreter.builtins.regex

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

class RegExpPrototype(interpreter: JavascriptInterpreter) : JavascriptObject(interpreter.objectPrototype) {
    override fun initialize() {
        super.initialize()

        setNonEnumerableNativeFunction("test") { executionContext ->
            val regexObject = executionContext.thisBinding as? RegExpObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val valueToTest = executionContext.arguments.first().toString()

            JavascriptValue.Boolean(Regex(regexObject.regex).matches(valueToTest))
        }

        setNonEnumerableNativeFunction("exec") { executionContext ->
            val regexObject = executionContext.thisBinding as? RegExpObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val valueToTest = executionContext.arguments.first().toString()

            regexObject.exec(valueToTest)
        }
    }
}
