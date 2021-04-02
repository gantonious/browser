package ca.antonious.browser.libraries.javascript.interpreter.builtins.regex

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

class RegExpObject(val regex: String, val flags: String) : JavascriptObject(RegExpPrototype) {

    override fun getProperty(key: String): JavascriptValue {
        return when (key) {
            "source" -> JavascriptValue.String(regex)
            else -> super.getProperty(key)
        }
    }

    override fun toString(): String {
        return "/$regex/$flags"
    }
}
