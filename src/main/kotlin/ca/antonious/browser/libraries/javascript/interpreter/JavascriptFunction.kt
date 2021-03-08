package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptStatement
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

sealed class JavascriptFunction {
    data class UserDefined(val functionStatement: JavascriptStatement.Function) : JavascriptFunction() {
        override fun toString(): String {
            return "function ${functionStatement.name}(${functionStatement.parameterNames.joinToString { ", "}}) { ... }"
        }
    }
    class Native(val body: (List<JavascriptValue>) -> JavascriptValue) : JavascriptFunction() {
        override fun toString() = "NativeFunction@${hashCode().toString(16)}"
    }
}