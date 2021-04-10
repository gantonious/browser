package ca.antonious.browser.libraries.web.javascript

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.web.layout.DOMParentLayoutNode

class JavascriptClassList(
    interpreter: JavascriptInterpreter,
    val domParentLayoutNode: DOMParentLayoutNode
) : JavascriptObject(interpreter.objectPrototype) {

    init {
        setNonEnumerableNativeFunction("add") { executionContext ->
            val className = executionContext.arguments.first().toString()
            val classList = domParentLayoutNode.htmlNode.attributes["class"]?.split(" ") ?: emptyList()
            val updatedClassList = classList.toSet() + className
            domParentLayoutNode.htmlNode.attributes["class"] = updatedClassList.joinToString(separator = " ").trim()
            JavascriptValue.Undefined
        }

        setNonEnumerableNativeFunction("remove") { executionContext ->
            val className = executionContext.arguments.first().toString()
            val classList = domParentLayoutNode.htmlNode.attributes["class"]?.split(" ") ?: emptyList()
            val updatedClassList = classList.toSet() - className
            domParentLayoutNode.htmlNode.attributes["class"] = updatedClassList.joinToString(separator = " ").trim()
            JavascriptValue.Undefined
        }
    }
}
