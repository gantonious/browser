package ca.antonious.browser.libraries.javascript.interpreter.builtins.`object`

import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.NativeFunction
import ca.antonious.browser.libraries.javascript.interpreter.builtins.array.JavascriptArray
import ca.antonious.browser.libraries.javascript.interpreter.builtins.number.NumberObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.string.StringObject
import ca.antonious.browser.libraries.javascript.interpreter.setNonEnumerableNativeFunction
import ca.antonious.browser.libraries.javascript.lexer.SourceInfo

class ObjectConstructor : NativeFunction(
    functionPrototype = ObjectPrototype,
    body = { executionContext ->
        when (val input = executionContext.arguments.first()) {
            is JavascriptValue.String -> JavascriptValue.Object(StringObject(input.value))
            is JavascriptValue.Number -> JavascriptValue.Object(NumberObject(input.value))
            else -> input
        }
    }
) {
    init {
        setNonEnumerableNativeFunction("keys") { executionContext ->
            val javascriptObject = executionContext.arguments.first().valueAs<JavascriptValue.Object>()

            if (javascriptObject == null) {
                JavascriptValue.Object(JavascriptArray())
            } else {
                JavascriptValue.Object(
                    JavascriptArray(
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
                JavascriptValue.Object(JavascriptArray())
            } else {
                JavascriptValue.Object(
                    JavascriptArray(
                        javascriptObject.value.properties.values.toList()
                    )
                )
            }
        }

        setNonEnumerableNativeFunction("getOwnPropertyNames") { executionContext ->
            val javascriptObject = executionContext.arguments.first().valueAs<JavascriptValue.Object>()

            if (javascriptObject == null) {
                JavascriptValue.Object(JavascriptArray())
            } else {
                JavascriptValue.Object(
                    JavascriptArray(
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
