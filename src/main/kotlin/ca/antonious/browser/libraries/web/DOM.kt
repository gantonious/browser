package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.layout.builtins.BlockNode
import ca.antonious.browser.libraries.layout.builtins.TextNode

class DOM {
    val rootNode = BlockNode()

    fun replaceDocument(htmlDocument: List<HtmlElement>) {
        rootNode.setChildren(htmlDocument.toDomElements().map { it.layoutNode })
    }
}

fun List<HtmlElement>.toDomElements(): List<DOMElement> {
    return map { it.toDomElement() }
}

fun HtmlElement.toDomElement(): DOMElement {
    return DOMElement(
        htmlElement = this,
        layoutNode = when (this) {
            is HtmlElement.Node -> BlockNode().apply {
                setChildren(this@toDomElement.children.toDomElements().map { it.layoutNode })
            }
            is HtmlElement.Text -> TextNode().apply {
                text = this@toDomElement.text
            }
        }
    )
}