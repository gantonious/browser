package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.builtins.`object`.ObjectPrototype

open class JavascriptObject(
    val prototype: JavascriptObject? = ObjectPrototype
) {

    val nonEnumerableProperties = mutableMapOf<String, JavascriptValue>()
    val properties = mutableMapOf<String, JavascriptValue>()

    val prototypeChain: List<JavascriptObject>
        get() {
            val chain = mutableListOf<JavascriptObject>()
            var currentPrototype = prototype

            while (currentPrototype != null) {
                chain += currentPrototype
                currentPrototype = currentPrototype.prototype
            }

            return chain
        }

    init {
        if (prototype != null) {
            setNonEnumerableProperty("__proto__", JavascriptValue.Object(prototype))
        }
    }

    open fun getProperty(key: String): JavascriptValue {
        return properties[key] ?: nonEnumerableProperties[key] ?: prototype?.getProperty(key)
            ?: JavascriptValue.Undefined
    }

    open fun setProperty(key: String, value: JavascriptValue) {
        if (key in nonEnumerableProperties) {
            nonEnumerableProperties[key] = value
            return
        }
        properties[key] = value
    }

    open fun deleteProperty(key: String) {
        properties.remove(key)
    }

    fun setNonEnumerableProperty(key: String, value: JavascriptValue) {
        nonEnumerableProperties[key] = value
    }

    override fun toString(): String {
        return "Object {${properties.entries.joinToString(separator = ", ") {
            val valueString = when (it.value) {
                is JavascriptValue.Object -> "Object"
                else -> it.value.toString()
            }
            "${it.key}: $valueString"
        }}}"
    }
}
