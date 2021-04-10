package ca.antonious.browser.libraries.javascript.interpreter.builtins.number

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

class NumberObject(interpreter: JavascriptInterpreter, val value: Double) : JavascriptObject(interpreter.numberPrototype) {
    override fun toString(): String {
        return value.toString()
    }
}
