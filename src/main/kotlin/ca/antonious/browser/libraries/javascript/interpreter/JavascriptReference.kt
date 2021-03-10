package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

data class JavascriptReference(val value: JavascriptValue, val setter: ((JavascriptValue) -> Unit)? = null) {
    companion object {
        val Undefined = JavascriptReference(value = JavascriptValue.Undefined)
    }
}

fun JavascriptValue.toReference(setter: ((JavascriptValue) -> Unit)? = null): JavascriptReference {
    return JavascriptReference(this, setter)
}