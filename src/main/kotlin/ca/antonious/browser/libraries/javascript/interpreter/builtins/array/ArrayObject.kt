package ca.antonious.browser.libraries.javascript.interpreter.builtins.array

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction

class ArrayObject(
    interpreter: JavascriptInterpreter,
    initialValues: List<JavascriptValue> = emptyList()
) : JavascriptObject(interpreter.arrayPrototype) {
    val array = initialValues.toMutableList()

    override fun getProperty(key: String): JavascriptValue {
        return when (key) {
            "length" -> JavascriptValue.Number(array.size.toDouble())
            "push" -> JavascriptValue.Object(
                value = NativeFunction(interpreter) { executionContext ->
                    array.add(executionContext.arguments.first())
                    JavascriptValue.Undefined
                }
            )
            "pop" -> JavascriptValue.Object(
                value = NativeFunction(interpreter) {
                    if (array.isEmpty()) {
                        JavascriptValue.Undefined
                    } else {
                        array.removeAt(array.size - 1)
                    }
                }
            )
            "sort" -> JavascriptValue.Object(
                value = NativeFunction(interpreter) {
                    array.sortBy { it.coerceToNumber() }
                    JavascriptValue.Object(this)
                }
            )
            "join" -> JavascriptValue.Object(
                value = NativeFunction(interpreter) { executionContext ->
                    val separator = executionContext.arguments.first().toString()
                    array.sortBy { it.coerceToNumber() }
                    JavascriptValue.String(array.joinToString(separator))
                }
            )
            else -> {
                val keyAsNumber = key.toDoubleOrNull()?.toInt()

                if (keyAsNumber == null) {
                    super.getProperty(key)
                } else if (keyAsNumber in 0 until array.size) {
                    array[keyAsNumber]
                } else {
                    JavascriptValue.Undefined
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
            addAll(enumerableProperties.map { "${it.first}: ${it.second}" })
        }
        return arrayWithObjectValues.toString()
    }
}

private data class Tuple(val left: Any, val right: Any) {
    override fun toString() = "$left: $right"
}
