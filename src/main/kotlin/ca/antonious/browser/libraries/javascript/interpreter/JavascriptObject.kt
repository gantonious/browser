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

    override fun toString() = properties.toString()
}

fun JavascriptObject.setNativeFunction(name: String, body: (List<JavascriptValue>) -> JavascriptValue) {
    setProperty(name, JavascriptValue.Function(value = JavascriptFunction.Native(body)))
}