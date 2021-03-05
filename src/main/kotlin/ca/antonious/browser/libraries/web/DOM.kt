package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.css.CssParser
import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.javascript.ast.JavascriptNode
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser
import ca.antonious.browser.libraries.layout.builtins.BlockNode
import ca.antonious.browser.libraries.web.layout.DOMLayoutNode
import ca.antonious.browser.libraries.web.layout.DOMParentLayoutNode
import ca.antonious.browser.libraries.web.layout.DOMTextNode

class DOM {
    val rootNode = BlockNode()
    private val cssStyleResolver = CssStyleResolver()
    private val javascriptInterpreter = JavascriptInterpreter()

    fun replaceDocument(htmlDocument: List<HtmlElement>) {
        val layoutTree = loadDocument(htmlDocument)
        resolveStyles(layoutTree)
        rootNode.setChildren(layoutTree)
    }

    private fun loadDocument(htmlDocument: List<HtmlElement>, parent: DOMLayoutNode? = null): List<DOMLayoutNode> {
        val layoutTree = mutableListOf<DOMLayoutNode>()

        for (htmlElement in htmlDocument) {
            when (htmlElement) {
                is HtmlElement.Node -> {
                    when (htmlElement.name) {
                        "head" -> processHead(htmlElement)
                        else -> {
                            val layoutNode = DOMParentLayoutNode(parent = parent, htmlElement = htmlElement)
                            layoutNode.setChildren(children = loadDocument(htmlElement.children, parent = layoutNode))
                            layoutTree += layoutNode
                        }
                    }
                }
                is HtmlElement.Text -> {
                    layoutTree += DOMTextNode(parent = parent, htmlElement = htmlElement)
                }
            }
        }

        return layoutTree
    }

    private fun processHead(node: HtmlElement.Node) {
        for (htmlElement in node.children) {
            if (htmlElement is HtmlElement.Node) {
                when (htmlElement.name) {
                    "style" -> {
                        val text = htmlElement.requireChildrenAsText().text
                        cssStyleResolver.addRules(CssParser().parse(text))
                    }
                    "script" -> {
                        val script = htmlElement.requireChildrenAsText().text
                        val parsedScript = JavascriptParser().parse(script)
                        javascriptInterpreter.interpret(JavascriptNode.Program(parsedScript))
                    }
                }
            }
        }
    }

    private fun resolveStyles(layoutTree: List<DOMLayoutNode>) {
        for (layoutNode in layoutTree) {
            when (layoutNode) {
                is DOMParentLayoutNode -> {
                    layoutNode.resolvedStyle = cssStyleResolver.resolveStyleFor(layoutNode)
                    resolveStyles(layoutNode.children)
                }
            }
        }
    }
}
