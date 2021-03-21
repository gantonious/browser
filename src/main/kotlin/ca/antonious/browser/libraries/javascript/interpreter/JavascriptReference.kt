package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

data class JavascriptReference(
    val value: JavascriptValue,
    val deleter: (() -> Unit)? = null,
    val setter: ((JavascriptValue) -> Unit)? = null
) {
    companion object {
        val Undefined = JavascriptReference(value = JavascriptValue.Undefined)
    }
}

fun JavascriptValue.toReference(deleter: (() -> Unit)? = null, setter: ((JavascriptValue) -> Unit)? = null): JavascriptReference {
    return JavascriptReference(this, deleter, setter)
}
