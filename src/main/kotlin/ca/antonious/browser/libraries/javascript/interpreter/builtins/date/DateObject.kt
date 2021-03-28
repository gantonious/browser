package ca.antonious.browser.libraries.javascript.interpreter.builtins.date

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import java.util.Date

class DateObject(val date: Date) : JavascriptObject() {
    override fun toString(): String {
        return date.toString()
    }
}
