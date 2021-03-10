package ca.antonious.browser.libraries.javascript.interpreter.builtins

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.setNativeFunction

class JavascriptArray(initialValues: List<JavascriptValue> = emptyList()) : JavascriptObject() {
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

    override fun getProperty(key: String): JavascriptValue {
        return when (key) {
            "length" -> JavascriptValue.Number(array.size.toDouble())
            else -> {
                val keyAsNumber = key.toIntOrNull()

                if (keyAsNumber == null) {
                    JavascriptValue.Undefined
                } else {
                    array[keyAsNumber]
                }
            }
        }
    }

    override fun toString(): String {
        return array.toString()
    }
}