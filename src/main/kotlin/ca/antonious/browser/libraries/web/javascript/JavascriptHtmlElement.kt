package ca.antonious.browser.libraries.web.javascript

import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.html.HtmlParser
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.array.JavascriptArray
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction
import ca.antonious.browser.libraries.web.layout.DOMLayoutNode
import ca.antonious.browser.libraries.web.layout.DOMParentLayoutNode
import ca.antonious.browser.libraries.web.layout.DOMTextNode

class JavascriptHtmlElement(
    private val domParentLayoutNode: DOMParentLayoutNode
) : JavascriptObject() {

    override fun setProperty(key: String, value: JavascriptValue) {
        when (key) {
            "id" -> {
                val id = value.toString()
                domParentLayoutNode.htmlNode.attributes["id"] = id
            }
            "className" -> {
                val className = value.toString()
                domParentLayoutNode.htmlNode.attributes["class"] = className
            }
            "innerHTML" -> {
                val html = value.toString()
                val parsedHtml = try {
                    HtmlParser().parse(html)
                } catch (ex: Exception) {
                    listOf(HtmlElement.Text(value.toString()))
                }

                domParentLayoutNode.children.clear()
                domParentLayoutNode.children += createDomNodes(parsedHtml)
                domParentLayoutNode.htmlNode.children.clear()
                domParentLayoutNode.htmlNode.children.addAll(parsedHtml)
            }
        }
    }

    private fun createDomNodes(htmlElements: List<HtmlElement>): List<DOMLayoutNode> {
        return htmlElements.map {
            when (it) {
                is HtmlElement.Node -> DOMParentLayoutNode(
                    parent = domParentLayoutNode,
                    domEventHandler = domParentLayoutNode.domEventHandler,
                    htmlNode = it
                ).apply {
                    setChildren(createDomNodes(it.children))
                }
                is HtmlElement.Text -> DOMTextNode(
                    parent = domParentLayoutNode,
                    htmlElement = it
                )
            }
        }
    }

    override fun getProperty(key: String): JavascriptValue {
        return when (key) {
            "id" -> {
                JavascriptValue.String(domParentLayoutNode.htmlNode.attributes["id"] ?: "")
            }
            "innerHTML" -> {
                if (domParentLayoutNode.children.size == 1 && domParentLayoutNode.children.first() is DOMTextNode) {
                    JavascriptValue.String((domParentLayoutNode.children.first() as DOMTextNode).htmlElement.requireAsText().text)
                } else {
                    JavascriptValue.String("")
                }
            }
            "value" -> JavascriptValue.Number(4.0)
            "appendChild" -> JavascriptValue.Object(
                NativeFunction { executionContext ->
                    val element = executionContext.arguments.first()
                        .valueAs<JavascriptValue.Object>()?.value as JavascriptHtmlElement
                    domParentLayoutNode.children += element.domParentLayoutNode
                    domParentLayoutNode.htmlNode.children.add(element.domParentLayoutNode.htmlNode)
                    JavascriptValue.Undefined
                }
            )
            "children" -> JavascriptValue.Object(
                JavascriptArray(
                    domParentLayoutNode.children.filterIsInstance<DOMParentLayoutNode>().map {
                        JavascriptValue.Object(JavascriptHtmlElement(it))
                    }
                )
            )
            "classList" -> {
                JavascriptValue.Object(JavascriptClassList(domParentLayoutNode))
            }
            else -> super.getProperty(key)
        }
    }
}
