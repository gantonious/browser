package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.css.CssParser
import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.html.HtmlParser
import ca.antonious.browser.libraries.http.*
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
    private val httpClient = HttpClient()
    private var siteUrl: Uri? = null

    private val htmlParser = HtmlParser()
    private val cssParser = CssParser()
    private val javascriptParser = JavascriptParser()

    fun loadSite(url: String) {
        siteUrl = url.toUri()
        val httpRequest = HttpRequest(url = siteUrl!!, method = HttpMethod.Get)
        httpClient.execute(httpRequest).onSuccess { response ->
            val rawHtml = response.body
            replaceDocument(htmlParser.parse(rawHtml))
        }
    }

    private fun replaceDocument(htmlDocument: List<HtmlElement>) {
        val layoutTree = loadDocument(htmlDocument)
        cssStyleResolver.reset()
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
                        cssStyleResolver.addRules(cssParser.parse(text))
                    }
                    "script" -> {
                        val src = htmlElement.attributes["src"]

                        if (src == null) {
                            val script = htmlElement.requireChildrenAsText().text
                            val parsedScript = javascriptParser.parse(script)
                            javascriptInterpreter.interpret(JavascriptNode.Program(parsedScript))
                        } else {
//                            httpClient.execute(HttpRequest(siteUrl!!.copy(path = src), HttpMethod.Get)).onSuccess { response ->
//                                javascriptInterpreter.interpret(JavascriptNode.Program(javascriptParser.parse(response.body)))
//                            }
                        }
                    }
                    "link" -> {
                        when (htmlElement.attributes["rel"]) {
                            "stylesheet" -> {
                                val href = htmlElement.attributes["href"]
                                val styleSheetUrl = siteUrl!!.copy(path = href!!)
                                httpClient.execute(HttpRequest(styleSheetUrl, HttpMethod.Get)).onSuccess { response ->
                                    cssStyleResolver.addRules(cssParser.parse(response.body))
                                    resolveStyles(rootNode.children.map { it as DOMLayoutNode })
                                }
                            }
                        }
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
