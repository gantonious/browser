package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.css.CssParser
import ca.antonious.browser.libraries.graphics.core.Color
import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.layout.builtins.BlockNode
import ca.antonious.browser.libraries.layout.builtins.TextNode
import com.jcraft.jorbis.Block

class DOM {
    val rootNode = BlockNode().apply {
        element = DOMElement(
            htmlElement = HtmlElement.Text(""),
            resolvedStyle = ResolvedStyle(),
            layoutNode = this
        )
    }
    val cssStyleResolver = CssStyleResolver()

    fun replaceDocument(htmlDocument: List<HtmlElement>) {
        htmlDocument.findHead()?.let(::processHead)
        rootNode.setChildren(htmlDocument.toDomElements().map { it.layoutNode })
    }

    private fun processHead(node: HtmlElement.Node) {
        for (child in node.children) {
            if (child is HtmlElement.Node) {
                when (child.name) {
                    "style" -> {
                        val text = child.requireChildrenAsText().text
                        cssStyleResolver.addRules(CssParser().parse(text))
                    }
                }
            }
        }
    }

    fun List<HtmlElement>.toDomElements(): List<DOMElement> {
        return filter { (it as? HtmlElement.Node)?.name != "head" }.map { it.toDomElement() }
    }

    fun HtmlElement.toDomElement(): DOMElement {
        return DOMElement(
            htmlElement = this,
            resolvedStyle = ResolvedStyle(),
            layoutNode = when (this) {
                is HtmlElement.Node -> BlockNode().apply {
                    setChildren(this@toDomElement.children.toDomElements().map { it.layoutNode })
                }
                is HtmlElement.Text -> TextNode().apply {
                    text = this@toDomElement.text
                }
            }
        ).apply {
            (layoutNode as? BlockNode)?.element = this
            copy(resolvedStyle = cssStyleResolver.resolveStyleFor(this))
        }
    }
}

fun List<HtmlElement>.findHead(): HtmlElement.Node? {
    val children = mutableListOf<HtmlElement>()

    forEach { element ->
        (element as? HtmlElement.Node)?.let {
            if (it.name == "head") {
                return it
            } else {
                children += it.children
            }
        }
    }

    return if (children.isEmpty()) {
        null
    } else {
        children.findHead()
    }
}

