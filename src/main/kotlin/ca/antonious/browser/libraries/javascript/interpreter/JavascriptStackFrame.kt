package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.shared.parsing.SourceInfo

data class JavascriptStackFrame(
    val name: String,
    var scope: JavascriptScope,
    var sourceInfo: SourceInfo
)
