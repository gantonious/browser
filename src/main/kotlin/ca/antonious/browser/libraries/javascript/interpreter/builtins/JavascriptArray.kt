package ca.antonious.browser.libraries.javascript.interpreter.builtins

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptReference
import ca.antonious.browser.libraries.javascript.interpreter.setNativeFunction
import ca.antonious.browser.libraries.javascript.interpreter.toReference

class JavascriptArray(initialValues: List<JavascriptValue> = emptyList()) : JavascriptObject() {
    private val array = initialValues.toMutableList()

    override fun getProperty(key: String): JavascriptValue {
        return when (key) {
            "length" -> JavascriptValue.Number(array.size.toDouble())
            "push" -> JavascriptValue.Function(
                value = JavascriptFunction.Native {
                    array.add(it.first())
                    JavascriptReference.Undefined
                }
            )
            "pop" -> JavascriptValue.Function(
                value = JavascriptFunction.Native {
                    if (array.isEmpty()) {
                        JavascriptReference.Undefined
                    } else {
                        array.removeAt(array.size - 1).toReference()
                    }
                }
            )
            else -> {
                val keyAsNumber = key.toDoubleOrNull()?.toInt()

                if (keyAsNumber == null) {
                    super.getProperty(key)
                } else {
                    array[keyAsNumber]
                }
            }
        }
    }

    override fun setProperty(key: String, value: JavascriptValue) {
        val keyAsNumber = key.toDoubleOrNull()?.toInt()

        if (keyAsNumber == null) {
            super.setProperty(key, value)
        } else {
            array[keyAsNumber] = value
        }
    }

    override fun toString(): String {
        val arrayWithObjectValues = mutableListOf<Any>().apply {
            addAll(array)
            addAll(properties.entries.map { Tuple(it.key, it.value) })
        }
        return arrayWithObjectValues.toString()
    }
}
private data class Tuple(val left: Any, val right: Any) {
    override fun toString() = "$left: $right"
}