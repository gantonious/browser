package ca.antonious.browser.libraries.javascript.interpreter.builtins.array

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeExecutionContext
import ca.antonious.browser.libraries.javascript.interpreter.testrunner.asA

class ArrayPrototype(interpreter: JavascriptInterpreter) : JavascriptObject(interpreter.objectPrototype) {
    override fun initialize() {
        setHigherOrderArrayFunction("forEach") { sourceArrayIterator ->
            sourceArrayIterator.invoke { _, _ -> true }
            JavascriptValue.Undefined
        }

        setHigherOrderArrayFunction("map") { sourceArrayIterator ->
            val outputArray = interpreter.makeArray()

            sourceArrayIterator.invoke { _, transformedValue ->
                outputArray.array += transformedValue
                true
            }

            JavascriptValue.Object(outputArray)
        }

        setHigherOrderArrayFunction("flatMap") { sourceArrayIterator ->
            val outputArray = interpreter.makeArray()

            sourceArrayIterator.invoke { _, transformedValue ->
                val transformedValueAsArray = transformedValue.asObject()?.asA<ArrayObject>()

                if (transformedValueAsArray != null) {
                    outputArray.array.addAll(transformedValueAsArray.array)
                } else {
                    outputArray.array += transformedValue
                }

                true
            }

            JavascriptValue.Object(outputArray)
        }

        setHigherOrderArrayFunction("filter") { sourceArrayIterator ->
            val outputArray = interpreter.makeArray()

            sourceArrayIterator.invoke { originalValue, transformedValue ->
                if (transformedValue.isTruthy) {
                    outputArray.array += originalValue
                }
                true
            }

            JavascriptValue.Object(outputArray)
        }

        setHigherOrderArrayFunction("find") { sourceArrayIterator ->
            var returnValue: JavascriptValue = JavascriptValue.Undefined

            sourceArrayIterator.invoke { originalValue, transformedValue ->
                if (transformedValue.isTruthy) {
                    returnValue = originalValue
                    false
                } else {
                    true
                }
            }

            returnValue
        }
    }

    private inline fun setHigherOrderArrayFunction(
        name: String,
        crossinline body: (sourceArrayIterator: ((JavascriptValue, JavascriptValue) -> Boolean) -> Unit) -> JavascriptValue
    ) {
        setNonEnumerableNativeFunction(name) { nativeExecutionContext ->
            val function = nativeExecutionContext.arguments.firstOrNull()?.asFunction()

            if (function === null) {
                nativeExecutionContext.interpreter.throwError(JavascriptValue.String("TypeError: Argument is not a function"))
                return@setNonEnumerableNativeFunction JavascriptValue.Undefined
            }

            val array = nativeExecutionContext.thisBinding as? ArrayObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val thisBinding = nativeExecutionContext.arguments.getOrNull(1)?.let { interpreter.interpretAsObject(it) }
                ?: nativeExecutionContext.interpreter.globalObject

            body { iterator ->
                for ((index, value) in array.array.withIndex()) {
                    val shouldContinue = iterator.invoke(
                        value,
                        function.call(
                            NativeExecutionContext(
                                callLocation = nativeExecutionContext.callLocation,
                                thisBinding = function.boundThis ?: thisBinding,
                                interpreter = nativeExecutionContext.interpreter,
                                arguments = listOf(
                                    value,
                                    JavascriptValue.Number(index.toDouble()),
                                    JavascriptValue.Object(array)
                                )
                            )
                        )
                    )

                    if (!shouldContinue) {
                        break
                    }
                }
            }
        }
    }
}
