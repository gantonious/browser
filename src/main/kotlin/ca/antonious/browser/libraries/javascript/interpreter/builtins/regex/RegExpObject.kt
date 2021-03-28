package ca.antonious.browser.libraries.javascript.interpreter.builtins.regex

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

class RegExpObject(val regex: String, val flags: String) : JavascriptObject() {
    override fun toString(): String {
        return "/$regex/$flags"
    }
}
