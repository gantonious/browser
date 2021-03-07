package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

class JavascriptArray(initialValues: List<JavascriptValue> = emptyList()) : JavascriptObject(null) {
    private val array = initialValues.toMutableList()

    init {
        setNativeFunction("push") {
            array.add(it.first())
            JavascriptValue.Undefined
        }

        setNativeFunction("pop") {
            if (array.isEmpty()) {
                JavascriptValue.Undefined
            } else {
                array.removeAt(array.size - 1)
            }
        }
    }

    override fun toString(): String {
        return array.toString()
    }
}