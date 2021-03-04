package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.MeasureTape
import ca.antonious.browser.libraries.graphics.core.Size
import ca.antonious.browser.libraries.html.HtmlParser
import ca.antonious.browser.libraries.http.HttpClient
import ca.antonious.browser.libraries.http.HttpMethod
import ca.antonious.browser.libraries.http.HttpRequest
import ca.antonious.browser.libraries.layout.core.LayoutConstraint
import ca.antonious.browser.libraries.layout.core.LayoutNode

class WebContentLayout(url: String) : LayoutNode() {
    private val dom = DOM()

    init {
        HttpClient().execute(HttpRequest(url, method = HttpMethod.Get)).onSuccess { response ->
            dom.replaceDocument(HtmlParser().parse(response.body))
        }
    }

    override fun measure(measureTape: MeasureTape, widthConstraint: LayoutConstraint, heightConstraint: LayoutConstraint): Size {
        return dom.rootNode.measure(measureTape, widthConstraint, heightConstraint)
    }

    override fun drawTo(canvas: Canvas) {
        dom.rootNode.drawTo(canvas)
    }
}