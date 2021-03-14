package ca.antonious.browser.libraries.javascript.interpreter.builtins

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

class ObjectObject : JavascriptObject() {
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