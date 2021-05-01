package ca.antonious.browser.libraries.javascript.interpreter.builtins.`object`

import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptPropertyDescriptor
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction
import ca.antonious.browser.libraries.javascript.interpreter.builtins.number.NumberObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.string.StringObject
import ca.antonious.browser.libraries.shared.parsing.SourceInfo

class ObjectConstructor(interpreter: JavascriptInterpreter) : NativeFunction(
    interpreter = interpreter,
    functionPrototype = interpreter.objectPrototype,
    body = { executionContext ->
        when (val input = executionContext.arguments.first()) {
            is JavascriptValue.String -> JavascriptValue.Object(StringObject(interpreter, input.value))
            is JavascriptValue.Number -> JavascriptValue.Object(NumberObject(interpreter, input.value))
            else -> input
        }
    }
) {
    init {
        setNonEnumerableNativeFunction("keys") { executionContext ->
            val javascriptObject = executionContext.arguments.first().valueAs<JavascriptValue.Object>()

            if (javascriptObject == null) {
                JavascriptValue.Object(interpreter.makeArray())
            } else {
                JavascriptValue.Object(
                    interpreter.makeArray(
                        javascriptObject.value.enumerableKeys.map {
                            JavascriptValue.String(it)
                        }
                    )
                )
            }
        }

        setNonEnumerableNativeFunction("values") { executionContext ->
            val javascriptObject = executionContext.arguments.first().valueAs<JavascriptValue.Object>()

            if (javascriptObject == null) {
                JavascriptValue.Object(interpreter.makeArray())
            } else {
                JavascriptValue.Object(
                    interpreter.makeArray(
                        javascriptObject.value.enumerableProperties.map { it.second }.toList()
                    )
                )
            }
        }

        setNonEnumerableNativeFunction("getOwnPropertyNames") { executionContext ->
            val javascriptObject = executionContext.arguments.first().valueAs<JavascriptValue.Object>()

            if (javascriptObject == null) {
                JavascriptValue.Object(interpreter.makeArray())
            } else {
                JavascriptValue.Object(
                    interpreter.makeArray(
                        (javascriptObject.value.allPropertyKeys).map {
                            JavascriptValue.String(it)
                        }
                    )
                )
            }
        }

        setNonEnumerableNativeFunction("getPrototypeOf") { executionContext ->
            val javascriptObject = executionContext.interpreter.interpretAsObject(
                JavascriptExpression.Literal(SourceInfo(0, 0), executionContext.arguments.first())
            )

            if (javascriptObject.prototype != null) {
                JavascriptValue.Object(javascriptObject.prototype)
            } else {
                JavascriptValue.Undefined
            }
        }

        setNonEnumerableNativeFunction("defineProperty") { executionContext ->
            val obj = executionContext.arguments.getOrNull(0)?.asObject() ?:
                return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val prop = executionContext.arguments.getOrNull(1)?.toString()
            val descriptor = executionContext.arguments.getOrNull(2)?.asObject()

            if (prop == null || descriptor == null) {
                executionContext.interpreter.throwTypeError("Property description must be an object: undefined")
                return@setNonEnumerableNativeFunction  JavascriptValue.Undefined
            }

            obj.setProperty(
                key = prop,
                descriptor = JavascriptPropertyDescriptor(
                    value = descriptor.getProperty("value"),
                    enumerable = descriptor.getProperty("enumerable").isTruthy,
                    writable = descriptor.getProperty("writable").isTruthy,
                    configurable = descriptor.getProperty("configurable").isTruthy
                )
            )

            JavascriptValue.Object(obj)
        }
    }
}
