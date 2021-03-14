package ca.antonious.browser.libraries.javascript.interpreter.builtins

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

class StringConstructor : NativeFunction(StringPrototype, { executionContext ->
    val stringValue = if (executionContext.arguments.isEmpty()) {
        ""
    } else {
        executionContext.arguments.first().toString()
    }

    if (executionContext.thisBinding.prototype == StringPrototype) {
        JavascriptValue.Object(StringObject(stringValue))
    } else {
        JavascriptValue.String(stringValue)
    }
})