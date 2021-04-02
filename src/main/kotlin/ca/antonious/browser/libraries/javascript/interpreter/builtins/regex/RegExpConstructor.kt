package ca.antonious.browser.libraries.javascript.interpreter.builtins.regex

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction
import ca.antonious.browser.libraries.javascript.interpreter.builtins.string.StringObject

class RegExpConstructor : NativeFunction(
    functionPrototype = RegExpPrototype,
    body = { executionContext ->
        val pattern = when (val patternObject = executionContext.interpreter.interpretAsObject(executionContext.arguments.first())) {
            is RegExpObject -> patternObject.regex
            is StringObject -> patternObject.value
            else -> patternObject.toString()
        }

        val flags = if (executionContext.arguments.size > 1) {
            executionContext.arguments[1].toString()
        } else {
            ""
        }

        JavascriptValue.Object(RegExpObject(pattern, flags))
    }
)
