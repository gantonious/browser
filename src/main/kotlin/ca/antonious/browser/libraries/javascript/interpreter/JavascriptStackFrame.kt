package ca.antonious.browser.libraries.javascript.interpreter

data class JavascriptStackFrame(
    val name: String,
    var scope: JavascriptScope
)
