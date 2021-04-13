package ca.antonious.browser.libraries.web.javascript

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction

class Element(interpreter: JavascriptInterpreter) : NativeFunction(
    interpreter = interpreter,
    functionPrototype = ElementPrototype(interpreter),
    body = { nativeExecutionContext ->
        nativeExecutionContext.interpreter.throwTypeError("Illegal constructor")
        JavascriptValue.Undefined
    }
)
