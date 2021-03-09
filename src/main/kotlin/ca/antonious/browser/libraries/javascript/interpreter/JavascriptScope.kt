package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

class JavascriptScope (
    private val thisBinding: JavascriptObject,
    private val scopeObject: JavascriptObject,
    private val parentScope: JavascriptScope?,
    val type: Type = Type.Block
) {
    init {
        scopeObject.setProperty("this", JavascriptValue.Object(thisBinding))
    }

    fun getProperty(key: String): JavascriptValue {
        return (
            scopeObject.getProperty(key) ?:
            parentScope?.getProperty(key) ?:
            thisBinding.getProperty(key)
        ) as JavascriptValue
    }

    fun setProperty(key: String, value: JavascriptValue) {
        scopeObject.setProperty(key, value)
    }

    sealed class Type {
        object Block : Type()
    }
}