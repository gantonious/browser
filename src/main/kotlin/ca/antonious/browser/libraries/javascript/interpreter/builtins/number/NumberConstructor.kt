package ca.antonious.browser.libraries.javascript.interpreter.builtins.number

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction

class NumberConstructor(interpreter: JavascriptInterpreter) : NativeFunction(
    interpreter = interpreter,
    functionPrototype = interpreter.numberPrototype,
    body = { executionContext ->
        val numberValue = if (executionContext.arguments.isEmpty()) {
            0.0
        } else {
            executionContext.arguments.first().coerceToNumber()
        }

        if (executionContext.thisBinding.prototype is NumberPrototype) {
            JavascriptValue.Object(NumberObject(interpreter, numberValue))
        } else {
            JavascriptValue.Number(numberValue)
        }
    }
) {
    init {
        setNonEnumerableNativeFunction("parseInt") { executionContext ->
            JavascriptValue.Number(executionContext.arguments.first().coerceToNumber().toInt().toDouble())
        }
    }
}
