package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.css.CssAttributeParser
import ca.antonious.browser.libraries.css.CssParser
import ca.antonious.browser.libraries.css.v2.tokenizer.CssTokenStream
import ca.antonious.browser.libraries.css.v2.tokenizer.RawCssInputStream
import ca.antonious.browser.libraries.graphics.images.ImageLoader
import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.html.parser.HtmlParser
import ca.antonious.browser.libraries.http.HttpClient
import ca.antonious.browser.libraries.http.HttpMethod
import ca.antonious.browser.libraries.http.HttpRequest
import ca.antonious.browser.libraries.http.Uri
import ca.antonious.browser.libraries.http.toUri
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.layout.builtins.BlockNode
import ca.antonious.browser.libraries.layout.core.Key
import ca.antonious.browser.libraries.layout.core.ApplicationExecutors
import ca.antonious.browser.libraries.web.javascript.Element
import ca.antonious.browser.libraries.web.javascript.JavascriptHtmlElement
import ca.antonious.browser.libraries.web.layout.DOMImageNode
import ca.antonious.browser.libraries.web.layout.DOMLayoutNode
import ca.antonious.browser.libraries.web.layout.DOMElementNode
import ca.antonious.browser.libraries.web.layout.DOMTextNode

class DOM {
    val rootNode = BlockNode()
    private val cssAttributeParser = CssAttributeParser()
    private val cssStyleResolver = CssStyleResolver()
    private val mainThreadExecutor = ApplicationExecutors.mainThreadExecutor()
    private val httpClient = HttpClient(responseExecutor = mainThreadExecutor)

    private var siteUrl: Uri? = null

    private val cssParser = CssParser()
    private val imageLoader = ImageLoader()

    private val javascriptInterpreter = JavascriptInterpreter(startDebugger = true).apply {
        val interpreter = this
        val documentObject = JavascriptValue.Object(
            makeObject().apply {
                setProperty("nodeType", JavascriptValue.Number(9.0))
                setProperty("documentElement", JavascriptValue.Object(this))

                setNonEnumerableNativeFunction("createElement") { nativeExecutionContext ->
                    val node = DOMElementNode(
                        parent = null,
                        htmlNode = HtmlElement.Node(name = nativeExecutionContext.arguments.firstOrNull().toString()),
                        domEventHandler = ::handleEvent
                    )
                    JavascriptValue.Object(JavascriptHtmlElement(interpreter, node))
                }

                setNonEnumerableNativeFunction("createDocumentFragment") { nativeExecutionContext ->
                    val node = DOMElementNode(
                        parent = null,
                        htmlNode = HtmlElement.Node(name = "div"),
                        domEventHandler = ::handleEvent
                    )
                    JavascriptValue.Object(JavascriptHtmlElement(interpreter, node))
                }

                setNonEnumerableNativeFunction("getElementsByClassName") { executionContext ->
                    val className = executionContext.arguments.first() as JavascriptValue.String
                    val matchingNodes =
                        findNodesWithClass(className.value, rootNode.children.map { it as DOMLayoutNode })
                    JavascriptValue.Object(
                        interpreter.makeArray(
                            matchingNodes.map {
                                JavascriptValue.Object(
                                    JavascriptHtmlElement(interpreter, it)
                                )
                            }
                        )
                    )
                }

                setNonEnumerableNativeFunction("getElementsByTagName") { executionContext ->
                    val tagName = executionContext.arguments.first() as JavascriptValue.String
                    val matchingNodes = rootNode.children.filterIsInstance<DOMElementNode>().flatMap { it.getElementsByTagName(tagName.value) }
                    JavascriptValue.Object(
                        interpreter.makeArray(
                            matchingNodes.map {
                                JavascriptValue.Object(
                                    JavascriptHtmlElement(interpreter, it)
                                )
                            }
                        )
                    )
                }

                setNonEnumerableNativeFunction("getElementById") { executionContext ->
                    val id = executionContext.arguments.first() as JavascriptValue.String
                    val node = findNodeWithId(id.value, rootNode.children.map { it as DOMLayoutNode })

                    if (node == null) {
                        JavascriptValue.Undefined
                    } else {
                        JavascriptValue.Object(JavascriptHtmlElement(interpreter, node))
                    }
                }

                setNonEnumerableNativeFunction("createElement") { executionContext ->
                    val tagName = executionContext.arguments.first() as JavascriptValue.String
                    val element = HtmlElement.Node(name = tagName.value)
                    val layoutNode = DOMElementNode(
                        parent = null,
                        domEventHandler = ::handleEvent,
                        htmlNode = element
                    )

                    JavascriptValue.Object(JavascriptHtmlElement(interpreter, layoutNode))
                }
            }
        )

        globalObject.setProperty(
            key = "window",
            value = JavascriptValue.Object(
                makeObject().apply {
                    setProperty("document", documentObject)
                    setNonEnumerableNativeFunction("onload") { JavascriptValue.Undefined }
                }
            )
        )

        globalObject.setProperty(
            key = "document",
            value = documentObject
        )

        globalObject.setProperty(
            key = "Element",
            value = JavascriptValue.Object(Element(this))
        )

        globalObject.setProperty("location", JavascriptValue.Object(makeObject().apply {
            setProperty("href", JavascriptValue.String(siteUrl.toString()))
        }))
    }

    fun loadSite(url: String) {
        loadSite(url.toUri())
    }

    fun loadSite(url: Uri) {
        siteUrl = url
        val httpRequest = HttpRequest(url = siteUrl!!, method = HttpMethod.Get)
        httpClient.execute(httpRequest).onSuccess { response ->
            val rawHtml = response.body
            val parser = HtmlParser(rawHtml)
            replaceDocument(listOf(parser.parse()))
        }
    }

    fun handleKeyDown(key: Key) {
        javascriptInterpreter.interpret("window.onkeydown && window.onkeydown({ code: '${key.name}' })")
    }

    private fun handleEvent(event: DOMEvent) {
        when (event) {
            is DOMEvent.NodeClicked -> {
                when {
                    event.element.name == "a" -> {
                        val url = siteUrl!!.copy(path = event.element.attributes["href"]!!)
                        loadSite(url)
                    }
                    event.element.attributes["onclick"] != null -> {
                        javascriptInterpreter.interpret(event.element.attributes["onclick"] ?: "")
                    }
                }
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

    private fun loadDocument(
        htmlDocument: List<HtmlElement>,
        parent: DOMElementNode? = null
    ): List<DOMLayoutNode> {
        val layoutTree = mutableListOf<DOMLayoutNode>()

        for (htmlElement in htmlDocument) {
            when (htmlElement) {
                is HtmlElement.Node -> {
                    when (htmlElement.name) {
                        "head" -> processHead(htmlElement)
                        "img" -> {
                            val layoutNode = DOMElementNode(
                                parent = parent,
                                htmlNode = htmlElement,
                                domEventHandler = ::handleEvent
                            )
                            val imageNode = DOMImageNode(
                                parent = layoutNode,
                                imgNode = htmlElement,
                                resolvedUrl = resolveUrl(htmlElement.attributes["src"] ?: "").toString(),
                                imageLoader = imageLoader
                            )

                            layoutNode.setChildren(children = listOf(imageNode))
                            layoutTree += layoutNode
                        }
                        "script" -> Unit
                        else -> {
                            val layoutNode = DOMElementNode(
                                parent = parent,
                                htmlNode = htmlElement,
                                domEventHandler = ::handleEvent
                            )
                            layoutNode.setChildren(children = loadDocument(htmlElement.children, parent = layoutNode))
                            layoutTree += layoutNode
                        }
                    }
                }
                is HtmlElement.Text -> {
                    layoutTree += DOMTextNode(
                        parent = parent,
                        htmlElement = htmlElement.copy(
                        text = htmlElement.text
                            .replace(Regex("[ \t]*\\n[ \t]*"), "")
                            .replace("\t", " ")
                            .replace("\n", " ")
                            .replace("  ", "")
                            .trim(' ')
                        )
                    )
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
                        try {
                            cssStyleResolver.addRules(cssParser.parse(text))
                        } catch (ex: Exception) {
                            println("CSS: Failed to parse inline style element because of: ${ex.message}")
                        }
                    }
                    "script" -> {
                        val src = htmlElement.attributes["src"]

                        if (src == null) {
                            val script = htmlElement.requireChildrenAsText().text
                            try {
                                javascriptInterpreter.interpret(script)
                            } catch (ex: Exception) {
                                println("JS: Failed to interpret inline script element because of: ${ex.message}")
                            }
                        } else {
                            httpClient.execute(HttpRequest(resolveUrl(src), HttpMethod.Get)).onSuccess { response ->
                                try {
                                    javascriptInterpreter.interpret(response.body, filename = src)
                                    javascriptInterpreter.interpret("window.onload()")
                                } catch (ex: Exception) {
                                    println("JS: Failed to interpret '$src' because of: ${ex.message}")
                                }
                            }
                        }
                    }
                    "link" -> {
                        when (htmlElement.attributes["rel"]) {
                            "stylesheet" -> {
                                val href = htmlElement.attributes["href"] ?: ""
                                val styleSheetUrl = resolveUrl(href)
                                if (styleSheetUrl.host.contains("google")) {
                                    continue
                                }
                                httpClient.execute(HttpRequest(styleSheetUrl, HttpMethod.Get)).onSuccess { response ->
                                    try {
                                        val tokenStream = CssTokenStream(input = RawCssInputStream(response.body))
                                        val parser = ca.antonious.browser.libraries.css.v2.tokenizer.CssParser()
                                        val styleSheet = parser.parseACssStyleSheet(tokenStream)
                                        print("CSS: V2 parser ran ${styleSheet.styleRules}")
                                    } catch (ex: Exception) {

                                    }
                                    try {
                                        cssStyleResolver.addRules(
                                            cssParser.parse(
                                                response.body.replace(
                                                    Regex("\\/\\*.*\\/"),
                                                    ""
                                                )
                                            )
                                        )
                                        resolveStyles(rootNode.children.map { it as DOMLayoutNode })
                                    } catch (ex: Exception) {
                                        println("CSS: Failed to parse '$styleSheetUrl' because of: ${ex.message}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun resolveStyles(layoutTree: List<DOMLayoutNode>) {
        for (layoutNode in layoutTree) {
            when (layoutNode) {
                is DOMElementNode -> {
                    val style = (layoutNode.htmlElement as HtmlElement.Node).attributes["style"] ?: ""
                    val inlineStyleAttributes = cssAttributeParser.parseInlineAttributes(style)
                    layoutNode.resolvedStyle = cssStyleResolver.resolveStyleFor(layoutNode, inlineStyleAttributes)
                    resolveStyles(layoutNode.children)
                }
            }
        }
    }

    private fun findNodesWithClass(className: String, nodes: List<DOMLayoutNode>): List<DOMElementNode> {
        val matchingElements = mutableListOf<DOMElementNode>()

        for (node in nodes) {
            when (node) {
                is DOMElementNode -> {
                    val htmlNode = node.htmlElement as HtmlElement.Node
                    if (className in (htmlNode.attributes["class"] ?: "").split(" ")) {
                        matchingElements += node
                    }

                    matchingElements += findNodesWithClass(className, node.children)
                }
            }
        }
        return matchingElements
    }

    private fun findNodeWithId(id: String, nodes: List<DOMLayoutNode>): DOMElementNode? {
        for (node in nodes) {
            when (node) {
                is DOMElementNode -> {
                    val htmlNode = node.htmlElement as HtmlElement.Node
                    if (htmlNode.attributes["id"] == id) {
                        return node
                    }

                    val nodeInChildren = findNodeWithId(id, node.children)
                    if (nodeInChildren != null) {
                        return nodeInChildren
                    }
                }
            }
        }

        return null
    }

    private fun resolveUrl(url: String): Uri {
        if (url.startsWith("http")) {
            return url.toUri()
        }

        if (url.startsWith("//")) {
            return url.replace("//", "${siteUrl?.scheme}://").toUri()
        }

        return siteUrl!!.uriForPath(url)
    }
}
