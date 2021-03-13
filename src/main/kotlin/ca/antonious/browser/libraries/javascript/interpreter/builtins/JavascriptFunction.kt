package ca.antonious.browser.libraries.javascript.interpreter.builtins

import ca.antonious.browser.libraries.javascript.ast.JavascriptStatement
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptScope

class JavascriptFunction(
    val parameterNames: List<String>,
    val body: JavascriptStatement.Block,
    val parentScope: JavascriptScope
) : JavascriptObject() {

    val functionPrototype = JavascriptObject()

    init {
        setNonEnumerableProperty("prototype", JavascriptValue.Object(functionPrototype))
    }

    override fun toString(): String {
        return "function (${parameterNames.joinToString(", ")}) {...}"
    }
}