package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

class JavascriptScope (
    val thisBinding: JavascriptObject,
    private val scopeObject: JavascriptObject,
    private val parentScope: JavascriptScope?,
    val type: Type = Type.Block
) {
    init {
        scopeObject.setProperty("this", JavascriptValue.Object(thisBinding))
    }

    fun getProperty(key: String): JavascriptValue {
        var returnValue = scopeObject.getProperty(key)

        if (returnValue == JavascriptValue.Undefined) {
            returnValue = parentScope?.getProperty(key) ?: JavascriptValue.Undefined
        }

        if (returnValue == JavascriptValue.Undefined) {
            returnValue = thisBinding.getProperty(key)
        }

        return returnValue
    }

    fun setProperty(key: String, value: JavascriptValue) {
        scopeObject.setProperty(key, value)
    }

    sealed class Type {
        object Block : Type()
    }
}