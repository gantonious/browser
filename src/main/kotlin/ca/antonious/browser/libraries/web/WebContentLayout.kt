package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.graphics.core.*
import ca.antonious.browser.libraries.html.HtmlParser
import ca.antonious.browser.libraries.http.HttpClient
import ca.antonious.browser.libraries.http.HttpMethod
import ca.antonious.browser.libraries.http.HttpRequest
import ca.antonious.browser.libraries.layout.core.InputEvent
import ca.antonious.browser.libraries.layout.core.LayoutConstraint
import ca.antonious.browser.libraries.layout.core.LayoutNode
import ca.antonious.browser.libraries.web.layout.DOMLayoutNode
import ca.antonious.browser.libraries.web.ui.NavigationBar

class WebContentLayout(url: String) : LayoutNode() {
    private val navigationBar = NavigationBar()
    private val dom = DOM()

    init {
        dom.loadSite(url = url)

        navigationBar.text = url
        navigationBar.onEnter = {
            dom.loadSite(it)
        }
    }

    override fun measure(measuringTape: MeasuringTape, widthConstraint: LayoutConstraint, heightConstraint: LayoutConstraint): Size {
        dom.resolveStyles(dom.rootNode.children.map { it as DOMLayoutNode })
        navigationBar.measure(measuringTape, widthConstraint, heightConstraint)
        return dom.rootNode.measure(measuringTape, widthConstraint, heightConstraint)
    }

    override fun drawTo(canvas: Canvas) {
        dom.rootNode.drawTo(canvas.subRegion(Rect(0f, navigationBar.frame.height, dom.rootNode.frame.width, dom.rootNode.frame.height)))
        navigationBar.drawTo(canvas)
    }

    override fun handleInputEvent(inputEvent: InputEvent) {
        if (inputEvent is InputEvent.KeyDown) {
            dom.handleKeyDown(inputEvent.key)
        }
        navigationBar.handleInputEvent(inputEvent)
        dom.rootNode.handleInputEvent(
            when (inputEvent) {
                is InputEvent.TouchUp -> InputEvent.TouchUp(
                    inputEvent.mousePosition.copy(y = inputEvent.mousePosition.y - navigationBar.frame.height)
                )
                else -> inputEvent
            }
        )
    }
}