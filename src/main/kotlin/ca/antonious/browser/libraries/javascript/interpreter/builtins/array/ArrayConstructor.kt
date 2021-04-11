package ca.antonious.browser.libraries.javascript.interpreter.builtins.array

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction

class ArrayConstructor(interpreter: JavascriptInterpreter) : NativeFunction(
    interpreter = interpreter,
    functionPrototype = ArrayPrototype(interpreter),
    body = { nativeExecutionContext ->
        JavascriptValue.Object(interpreter.makeArray(nativeExecutionContext.arguments))
    }
) {
    init {
        setNonEnumerableNativeFunction("isArray") { nativeExecutionContext ->
            JavascriptValue.Boolean(nativeExecutionContext.arguments.firstOrNull()?.asObject() is ArrayObject)
        }
    }
}
