package ca.antonious.browser.libraries.web.javascript

import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.html.HtmlParser
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptReference
import ca.antonious.browser.libraries.javascript.interpreter.builtins.JavascriptArray
import ca.antonious.browser.libraries.javascript.interpreter.builtins.JavascriptFunction
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
                domParentLayoutNode.htmlNode.attributes["className"] = className
            }
            "innerHTML" -> {
                val html = value.toString()
                val parsedHtml = if (html.isEmpty()) {
                    emptyList()
                } else {
                    HtmlParser().parse(html)
                }
                val childrenNodes = parsedHtml.map {
                    when (it) {
                        is HtmlElement.Node -> DOMParentLayoutNode(
                            parent = domParentLayoutNode,
                            domEventHandler = domParentLayoutNode.domEventHandler,
                            htmlNode = it
                        )
                        is HtmlElement.Text -> DOMTextNode(
                            parent = domParentLayoutNode,
                            htmlElement = it
                        )
                    }
                }
                domParentLayoutNode.children += childrenNodes
                domParentLayoutNode.htmlNode.children.addAll(parsedHtml)
            }
        }
    }

    override fun getProperty(key: String): JavascriptValue {
        return when (key) {
            "value" -> JavascriptValue.Number(4.0)
            "appendChild" -> JavascriptValue.Function(
                JavascriptFunction.Native { args ->
                    val element = args.first().valueAs<JavascriptValue.Object>()?.value as JavascriptHtmlElement
                    domParentLayoutNode.children += element.domParentLayoutNode
                    domParentLayoutNode.htmlNode.children.add(element.domParentLayoutNode.htmlNode)
                    JavascriptReference.Undefined
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
            else -> JavascriptValue.Undefined
        }
    }
}