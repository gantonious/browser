package ca.antonious.browser.libraries.javascript.interpreter.builtins.date

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction
import java.text.DateFormat
import java.util.Date

class DateConstructor(interpreter: JavascriptInterpreter) : NativeFunction(
    interpreter = interpreter,
    functionPrototype = interpreter.datePrototype,
    body = { executionContext ->
        val date = if (executionContext.arguments.isEmpty()) {
            Date()
        } else {
            DateFormat.getDateInstance().parse(executionContext.arguments.first().toString())
        }

        JavascriptValue.Object(DateObject(interpreter, date))
    }
)
