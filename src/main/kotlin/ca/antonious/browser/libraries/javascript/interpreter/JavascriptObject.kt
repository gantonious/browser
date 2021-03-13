package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.builtins.JavascriptFunction

open class JavascriptObject {

    private val nonEnumerableProperties = mutableMapOf<String, JavascriptValue>()
    protected val properties = mutableMapOf<String, JavascriptValue>()

    open fun getProperty(key: String): JavascriptValue {
        return properties[key] ?: nonEnumerableProperties[key] ?: JavascriptValue.Undefined
    }

    open fun setProperty(key: String, value: JavascriptValue) {
        properties[key] = value
    }

    open fun setNonEnumerableProperty(key: String, value: JavascriptValue) {
        nonEnumerableProperties[key] = value
    }

    override fun toString(): String {
        return "Object {${properties.entries.joinToString(separator = ", ") { "${it.key}: ${it.value}" }}}"
    }
}