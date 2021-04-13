package ca.antonious.browser.libraries.web.javascript

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.web.layout.DOMElementNode

class JavascriptClassList(
    interpreter: JavascriptInterpreter,
    val domElementNode: DOMElementNode
) : JavascriptObject(interpreter.objectPrototype) {

    init {
        setNonEnumerableNativeFunction("add") { executionContext ->
            val className = executionContext.arguments.first().toString()
            val classList = domElementNode.htmlNode.attributes["class"]?.split(" ") ?: emptyList()
            val updatedClassList = classList.toSet() + className
            domElementNode.htmlNode.attributes["class"] = updatedClassList.joinToString(separator = " ").trim()
            JavascriptValue.Undefined
        }

        setNonEnumerableNativeFunction("remove") { executionContext ->
            val className = executionContext.arguments.first().toString()
            val classList = domElementNode.htmlNode.attributes["class"]?.split(" ") ?: emptyList()
            val updatedClassList = classList.toSet() - className
            domElementNode.htmlNode.attributes["class"] = updatedClassList.joinToString(separator = " ").trim()
            JavascriptValue.Undefined
        }
    }
}
