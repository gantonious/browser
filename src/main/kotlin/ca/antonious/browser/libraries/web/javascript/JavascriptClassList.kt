package ca.antonious.browser.libraries.web.javascript

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptReference
import ca.antonious.browser.libraries.javascript.interpreter.builtins.JavascriptFunction
import ca.antonious.browser.libraries.web.layout.DOMParentLayoutNode

class JavascriptClassList(
    val domParentLayoutNode: DOMParentLayoutNode
) : JavascriptObject() {
    override fun getProperty(key: String): JavascriptValue {
        return when (key) {
            "add" -> JavascriptValue.Function(
                JavascriptFunction.Native { args ->
                    val className = args.first().toString()
                    val classList = domParentLayoutNode.htmlNode.attributes["class"]?.split(" ") ?: emptyList()
                    val updatedClassList = classList.toSet() + className
                    domParentLayoutNode.htmlNode.attributes["class"] = updatedClassList.joinToString(separator = " ").trim()
                    JavascriptReference.Undefined
                }
            )
            "remove" -> JavascriptValue.Function(
                JavascriptFunction.Native { args ->
                    val className = args.first().toString()
                    val classList = domParentLayoutNode.htmlNode.attributes["class"]?.split(" ") ?: emptyList()
                    val updatedClassList = classList.toSet() - className
                    domParentLayoutNode.htmlNode.attributes["class"] = updatedClassList.joinToString(separator = " ").trim()
                    JavascriptReference.Undefined
                }
            )
            else -> JavascriptValue.Undefined
        }
    }


}