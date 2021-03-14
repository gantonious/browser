package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.builtins.`object`.ObjectPrototype

open class JavascriptObject(
    val prototype: JavascriptObject? = ObjectPrototype
) {

    val nonEnumerableProperties = mutableMapOf<String, JavascriptValue>()
    val properties = mutableMapOf<String, JavascriptValue>()

    init {
        if (prototype != null) {
            setNonEnumerableProperty("__proto__", JavascriptValue.Object(prototype))
        }
    }

    open fun getProperty(key: String): JavascriptValue {
        return properties[key] ?: nonEnumerableProperties[key] ?: prototype?.getProperty(key) ?: JavascriptValue.Undefined
    }

    open fun setProperty(key: String, value: JavascriptValue) {
        properties[key] = value
    }

    fun setNonEnumerableProperty(key: String, value: JavascriptValue) {
        nonEnumerableProperties[key] = value
    }

    override fun toString(): String {
        return "Object {${properties.entries.joinToString(separator = ", ") { "${it.key}: ${it.value}" }}}"
    }
}