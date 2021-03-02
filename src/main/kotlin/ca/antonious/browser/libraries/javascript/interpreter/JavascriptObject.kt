package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

class JavascriptObject(val parent: JavascriptObject? = null) {

    private val properties = mutableMapOf<String, Any?>()

    fun getProperty(key: String): Any? {
        return properties[key] ?: parent?.getProperty(key)
    }

    fun setProperty(key: String, value: Any?) {
        properties[key] = value
    }

    override fun toString(): String {
        val newLineIfHasProperties = if (properties.isNotEmpty()) "\n" else ""

        return "Object {$newLineIfHasProperties${properties.entries.joinToString(separator = ",\n") { "    ${it.key}: ${it.value}" }}$newLineIfHasProperties}"
    }
}

fun JavascriptObject.setNativeFunction(name: String, body: (List<JavascriptValue>) -> JavascriptValue) {
    setProperty(name, JavascriptValue.Function(value = JavascriptFunction.Native(body)))
}