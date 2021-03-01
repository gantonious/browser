package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

class NativeFunction(val body: (List<JavascriptValue>) -> JavascriptValue)