package ca.antonious.browser.libraries.javascript.interpreter.builtins.number

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

class NumberObject(val value: Double) : JavascriptObject(NumberPrototype) {
    override fun toString(): String {
        return value.toString()
    }
}
