package ca.antonious.browser.libraries.javascript.interpreter.builtins.string

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

class StringObject(val value: String) : JavascriptObject(StringPrototype) {
    override fun toString(): String {
        return value
    }
}
