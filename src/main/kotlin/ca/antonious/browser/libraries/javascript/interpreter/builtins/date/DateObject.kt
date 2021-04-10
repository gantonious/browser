package ca.antonious.browser.libraries.javascript.interpreter.builtins.date

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import java.util.Date

class DateObject(interpreter: JavascriptInterpreter, val date: Date) : JavascriptObject(interpreter.datePrototype) {
    override fun toString(): String {
        return date.toString()
    }
}
