package ca.antonious.browser.libraries.javascript.interpreter.builtins.number

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.setNonEnumerableNativeFunction

class NumberConstructor : NativeFunction(
    functionPrototype = NumberPrototype,
    body = { executionContext ->
        val numberValue = if (executionContext.arguments.isEmpty()) {
            0.0
        } else {
            executionContext.arguments.first().coerceToNumber()
        }

        if (executionContext.thisBinding.prototype == NumberPrototype) {
            JavascriptValue.Object(NumberObject(numberValue))
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
