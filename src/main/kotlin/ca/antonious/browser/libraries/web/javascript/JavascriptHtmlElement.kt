package ca.antonious.browser.libraries.web.javascript

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.web.layout.DOMParentLayoutNode

class JavascriptHtmlElement(val domParentLayoutNode: DOMParentLayoutNode) : JavascriptObject() {
    override fun setProperty(key: String, value: JavascriptValue) {
        print("HTMLElement setting $key to $value")
        super.setProperty(key, value)
    }

    override fun getProperty(key: String): JavascriptValue {
        return when (key) {
            "value" -> JavascriptValue.Number(4.0)
            else -> JavascriptValue.Undefined
        }
    }
}