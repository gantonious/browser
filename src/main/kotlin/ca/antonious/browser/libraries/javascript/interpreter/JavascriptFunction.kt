package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptStatement
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

class JavascriptFunction(
    val name: String,
    val parameterNames: List<String>,
    val body: JavascriptStatement.Block,
    val parentScope: JavascriptScope
) : JavascriptObject() {

    val functionPrototype: JavascriptObject
        get() {
            return getProperty("prototype").valueAs<JavascriptValue.Object>()?.value ?: JavascriptObject()
        }

    init {
        setNonEnumerableProperty("prototype", JavascriptValue.Object(JavascriptObject()))
    }

    override fun toString(): String {
        return "function (${parameterNames.joinToString(", ")}) {...}"
    }
}
