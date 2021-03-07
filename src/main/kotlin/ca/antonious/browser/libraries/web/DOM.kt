package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.css.CssAttributeParser
import ca.antonious.browser.libraries.css.CssParser
import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.html.HtmlParser
import ca.antonious.browser.libraries.http.*
import ca.antonious.browser.libraries.javascript.ast.JavascriptNode
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.setNativeFunction
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser
import ca.antonious.browser.libraries.layout.builtins.BlockNode
import ca.antonious.browser.libraries.web.layout.DOMLayoutNode
import ca.antonious.browser.libraries.web.layout.DOMParentLayoutNode
import ca.antonious.browser.libraries.web.layout.DOMTextNode

class DOM {
    val rootNode = BlockNode()
    private val cssAttributeParser = CssAttributeParser()
    private val cssStyleResolver = CssStyleResolver()

    private val httpClient = HttpClient()
    private var siteUrl: Uri? = null

    private val htmlParser = HtmlParser()
    private val cssParser = CssParser()
    private val javascriptParser = JavascriptParser()

    private val javascriptInterpreter = JavascriptInterpreter().apply {
        globalObject.setProperty(
            key = "window",
            value = JavascriptValue.Object(
                JavascriptObject().apply {
                    setNativeFunction("onload") { JavascriptValue.Undefined }
                }
            )
        )
    }

    fun loadSite(url: String) {
        loadSite(url.toUri())
    }

    fun loadSite(url: Uri) {
        siteUrl = url
        val httpRequest = HttpRequest(url = siteUrl!!, method = HttpMethod.Get)
        httpClient.execute(httpRequest).onSuccess { response ->
            val rawHtml = response.body
            replaceDocument(htmlParser.parse(rawHtml))
        }
    }

    private fun handleEvent(event: DOMEvent) {
        when (event) {
            is DOMEvent.NodeClicked -> {
                val url = siteUrl!!.copy(path = event.element.attributes["href"]!!)
                loadSite(url)
            }
        }
    }

    private fun replaceDocument(htmlDocument: List<HtmlElement>) {
        val layoutTree = loadDocument(htmlDocument)
        cssStyleResolver.reset()
        resolveStyles(layoutTree)
        rootNode.setChildren(layoutTree)
        javascriptInterpreter.interpret("window.onload()")
    }

    private fun loadDocument(htmlDocument: List<HtmlElement>, parent: DOMParentLayoutNode? = null): List<DOMLayoutNode> {
        val layoutTree = mutableListOf<DOMLayoutNode>()

        for (htmlElement in htmlDocument) {
            when (htmlElement) {
                is HtmlElement.Node -> {
                    when (htmlElement.name) {
                        "head" -> processHead(htmlElement)
                        else -> {
                            val layoutNode = DOMParentLayoutNode(parent = parent, htmlElement = htmlElement, domEventHandler = ::handleEvent)
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
                    val style = (layoutNode.htmlElement as HtmlElement.Node).attributes["style"] ?: ""
                    val inlineStyleAttributes = cssAttributeParser.parseInlineAttributes(style)
                    layoutNode.resolvedStyle = cssStyleResolver.resolveStyleFor(layoutNode, inlineStyleAttributes)
                    resolveStyles(layoutNode.children)
                }
            }
        }
    }
}
