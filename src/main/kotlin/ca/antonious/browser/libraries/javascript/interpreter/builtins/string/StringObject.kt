package ca.antonious.browser.libraries.javascript.interpreter.builtins.string

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

class StringObject(interpreter: JavascriptInterpreter, val value: String) : JavascriptObject(interpreter.stringPrototype) {

    override fun getProperty(key: String): JavascriptValue {
        return when (key) {
            "length" -> JavascriptValue.Number(value.length.toDouble())
            else -> super.getProperty(key)
        }
    }
    override fun toString(): String {
        return value
    }
}
