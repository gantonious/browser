package ca.antonious.browser.libraries.javascript.interpreter.builtins

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

object StringPrototype : JavascriptObject() {
    init {
        setNonEnumerableNativeFunction("valueOf") { executionContext ->
            JavascriptValue.String((executionContext.thisBinding as StringObject).value)
        }

        setNonEnumerableNativeFunction("match") { executionContext ->
            val stringObject = executionContext.thisBinding as? StringObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val regex = executionContext.arguments.first().valueAs<JavascriptValue.Object>()?.value as JavascriptRegex
            JavascriptValue.Object(
                JavascriptArray(
                    Regex(regex.regex).findAll(stringObject.value).map { JavascriptValue.String(it.value) }.toList()
                )
            )
        }

        setNonEnumerableNativeFunction("toLowerCase") { executionContext ->
            val stringObject = executionContext.thisBinding as? StringObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            JavascriptValue.String(stringObject.value.toLowerCase())
        }

        setNonEnumerableNativeFunction("toUpperCase") { executionContext ->
            val stringObject = executionContext.thisBinding as? StringObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            JavascriptValue.String(stringObject.value.toUpperCase())
        }
    }
}