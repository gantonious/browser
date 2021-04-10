package ca.antonious.browser.libraries.javascript.interpreter.builtins.`object`

import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction
import ca.antonious.browser.libraries.javascript.interpreter.builtins.number.NumberObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.string.StringObject
import ca.antonious.browser.libraries.javascript.lexer.SourceInfo

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
                        javascriptObject.value.properties.keys.map {
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
                        javascriptObject.value.properties.values.toList()
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
                        (javascriptObject.value.properties.keys + javascriptObject.value.nonEnumerableProperties.keys).map {
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
    }
}
