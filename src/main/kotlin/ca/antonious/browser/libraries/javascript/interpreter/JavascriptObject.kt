package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.builtins.JavascriptFunction

open class JavascriptObject {

    protected val properties = mutableMapOf<String, JavascriptValue>()

    open fun getProperty(key: String): JavascriptValue {
        return properties[key] ?: JavascriptValue.Undefined
    }

    open fun setProperty(key: String, value: JavascriptValue) {
        properties[key] = value
    }

    override fun toString(): String {
        return "Object {${properties.entries.joinToString(separator = ", ") { "${it.key}: ${it.value}" }}}"
    }
}

fun JavascriptObject.setNativeFunction(name: String, body: (List<JavascriptValue>) -> JavascriptValue) {
    setProperty(
        key = name,
        value = JavascriptValue.Function(
            value = JavascriptFunction.Native { body(it).toReference() }
        )
    )
}