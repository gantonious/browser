package ca.antonious.browser.libraries.javascript.interpreter.builtins.string

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction

class StringConstructor(interpreter: JavascriptInterpreter) : NativeFunction(
    interpreter = interpreter,
    functionPrototype = interpreter.stringPrototype,
    body = { executionContext ->
        val stringValue = if (executionContext.arguments.isEmpty()) {
            ""
        } else {
            executionContext.arguments.first().toString()
        }

        if (executionContext.thisBinding.prototype is StringPrototype) {
            JavascriptValue.Object(StringObject(interpreter, stringValue))
        } else {
            JavascriptValue.String(stringValue)
        }
    }
)
