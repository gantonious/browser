package ca.antonious.browser.libraries.javascript.interpreter.builtins.`object`

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.NativeFunction
import ca.antonious.browser.libraries.javascript.interpreter.builtins.array.JavascriptArray
import ca.antonious.browser.libraries.javascript.interpreter.builtins.number.NumberObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.string.StringObject
import ca.antonious.browser.libraries.javascript.interpreter.setNonEnumerableNativeFunction

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
    }
}
