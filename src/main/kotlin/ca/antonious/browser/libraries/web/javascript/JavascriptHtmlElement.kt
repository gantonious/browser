package ca.antonious.browser.libraries.web.javascript

import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.html.v2.parser.HtmlParser
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction
import ca.antonious.browser.libraries.web.layout.DOMLayoutNode
import ca.antonious.browser.libraries.web.layout.DOMElementNode
import ca.antonious.browser.libraries.web.layout.DOMTextNode

class JavascriptHtmlElement(
    interpreter: JavascriptInterpreter,
    val domElementNode: DOMElementNode
) : JavascriptObject(interpreter.elementPrototype) {

    override fun setProperty(key: String, value: JavascriptValue) {
        when (key) {
            "id" -> {
                val id = value.toString()
                domElementNode.htmlNode.attributes["id"] = id
            }
            "className" -> {
                val className = value.toString()
                domElementNode.htmlNode.attributes["class"] = className
            }
            "innerHTML" -> {
                val parsedHtml = HtmlParser(value.toString()).parseAsFragment(domElementNode.htmlNode)

                domElementNode.children.clear()
                domElementNode.children += createDomNodes(parsedHtml)
                domElementNode.htmlNode.children.clear()
                domElementNode.htmlNode.children.addAll(parsedHtml)
            }
        }
    }

    private fun createDomNodes(htmlElements: List<HtmlElement>): List<DOMLayoutNode> {
        return htmlElements.map {
            when (it) {
                is HtmlElement.Node -> DOMElementNode(
                    parent = domElementNode,
                    domEventHandler = domElementNode.domEventHandler,
                    htmlNode = it
                ).apply {
                    setChildren(createDomNodes(it.children))
                }
                is HtmlElement.Text -> DOMTextNode(
                    parent = domElementNode,
                    htmlElement = it
                )
            }
        }
    }

    override fun getProperty(key: String): JavascriptValue {
        return when (key) {
            "id" -> {
                JavascriptValue.String(domElementNode.htmlNode.attributes["id"] ?: "")
            }
            "innerHTML" -> {
                if (domElementNode.children.size == 1 && domElementNode.children.first() is DOMTextNode) {
                    JavascriptValue.String((domElementNode.children.first() as DOMTextNode).htmlElement.requireAsText().text)
                } else {
                    JavascriptValue.String("")
                }
            }
            "value" -> JavascriptValue.Number(4.0)
            "appendChild" -> JavascriptValue.Object(
                NativeFunction(interpreter) { executionContext ->
                    val element = executionContext.arguments.first()
                        .valueAs<JavascriptValue.Object>()?.value as JavascriptHtmlElement
                    domElementNode.children += element.domElementNode
                    domElementNode.htmlNode.children.add(element.domElementNode.htmlNode)
                    JavascriptValue.Undefined
                }
            )
            "children" -> JavascriptValue.Object(
                interpreter.makeArray(
                    domElementNode.children.filterIsInstance<DOMElementNode>().map {
                        JavascriptValue.Object(JavascriptHtmlElement(interpreter, it))
                    }
                )
            )
            "classList" -> {
                JavascriptValue.Object(JavascriptClassList(interpreter, domElementNode))
            }
            else -> super.getProperty(key)
        }
    }
}
