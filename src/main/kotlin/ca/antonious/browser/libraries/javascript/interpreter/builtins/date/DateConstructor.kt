package ca.antonious.browser.libraries.javascript.interpreter.builtins.date

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction
import java.text.DateFormat
import java.util.Date

class DateConstructor : NativeFunction(
    body = { executionContext ->
        val date = if (executionContext.arguments.isEmpty()) {
            Date()
        } else {
            DateFormat.getDateInstance().parse(executionContext.arguments.first().toString())
        }


        JavascriptValue.Object(DateObject(date))
    }
)
