package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptNode
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

sealed class JavascriptFunction {
    data class UserDefined(val functionNode: JavascriptNode.Function) : JavascriptFunction() {
        override fun toString(): String {
            return "function ${functionNode.name}(${functionNode.parameterNames.joinToString { ", "}}) { ... }"
        }
    }
    class Native(val body: (List<JavascriptValue>) -> JavascriptValue) : JavascriptFunction() {
        override fun toString() = "NativeFunction@${hashCode().toString(16)}"
    }
}