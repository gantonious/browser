package ca.antonious.browser.libraries.javascript.interpreter.builtins

import ca.antonious.browser.libraries.javascript.ast.JavascriptStatement
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptReference
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptScope

sealed class JavascriptFunction {
    data class UserDefined(
        val parameterNames: List<String>,
        val body: JavascriptStatement.Block,
        val parentScope: JavascriptScope
    ) : JavascriptFunction() {
        override fun toString(): String {
            return "function (${parameterNames.joinToString { ", "}}) { ... }"
        }
    }
    class Native(val body: (List<JavascriptValue>) -> JavascriptReference) : JavascriptFunction() {
        override fun toString() = "NativeFunction@${hashCode().toString(16)}"
    }
}