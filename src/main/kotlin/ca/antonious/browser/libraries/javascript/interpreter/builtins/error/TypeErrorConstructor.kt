package ca.antonious.browser.libraries.javascript.interpreter.builtins.error

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction

class TypeErrorConstructor(interpreter: JavascriptInterpreter) : NativeFunction(
    interpreter = interpreter,
    functionPrototype = interpreter.typeErrorPrototype,
    body = { nativeExecutionContext ->
        val message = nativeExecutionContext.interpreter.interpretAsString(
            nativeExecutionContext.arguments.firstOrNull() ?: JavascriptValue.Undefined
        )

        JavascriptValue.Object(
            nativeExecutionContext.interpreter.makeObject(interpreter.typeErrorPrototype).apply {
                setProperty("message", JavascriptValue.String(message))
            }
        )
    }
)
