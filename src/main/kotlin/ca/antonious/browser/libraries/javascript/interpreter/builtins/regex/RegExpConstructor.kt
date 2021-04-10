package ca.antonious.browser.libraries.javascript.interpreter.builtins.regex

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction
import ca.antonious.browser.libraries.javascript.interpreter.builtins.string.StringObject

class RegExpConstructor(interpreter: JavascriptInterpreter) : NativeFunction(
    interpreter = interpreter,
    functionPrototype = interpreter.regExpPrototype,
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

        JavascriptValue.Object(RegExpObject(interpreter, pattern, flags))
    }
)
