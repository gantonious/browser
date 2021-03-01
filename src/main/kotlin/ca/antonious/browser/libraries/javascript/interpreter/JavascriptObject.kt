package ca.antonious.browser.libraries.javascript.interpreter

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