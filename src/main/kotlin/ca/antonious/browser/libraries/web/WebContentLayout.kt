package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.MeasuringTape
import ca.antonious.browser.libraries.graphics.core.Size
import ca.antonious.browser.libraries.html.HtmlParser
import ca.antonious.browser.libraries.http.HttpClient
import ca.antonious.browser.libraries.http.HttpMethod
import ca.antonious.browser.libraries.http.HttpRequest
import ca.antonious.browser.libraries.layout.core.InputEvent
import ca.antonious.browser.libraries.layout.core.LayoutConstraint
import ca.antonious.browser.libraries.layout.core.LayoutNode

class WebContentLayout(url: String) : LayoutNode() {
    private val dom = DOM()

    init {
        dom.loadSite(url = url)
    }

    override fun measure(measuringTape: MeasuringTape, widthConstraint: LayoutConstraint, heightConstraint: LayoutConstraint): Size {
        return dom.rootNode.measure(measuringTape, widthConstraint, heightConstraint)
    }

    override fun drawTo(canvas: Canvas) {
        dom.rootNode.drawTo(canvas)
    }

    override fun handleInputEvent(inputEvent: InputEvent) {
        dom.rootNode.handleInputEvent(inputEvent)
    }
}