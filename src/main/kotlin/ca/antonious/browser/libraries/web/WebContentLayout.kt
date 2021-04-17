package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.graphics.core.*
import ca.antonious.browser.libraries.layout.builtins.VStack
import ca.antonious.browser.libraries.layout.core.InputEvent
import ca.antonious.browser.libraries.layout.core.LayoutNode
import ca.antonious.browser.libraries.web.layout.DOMLayoutNode
import ca.antonious.browser.libraries.web.ui.NavigationBar

class WebContentLayout(url: String) : LayoutNode() {
    private val navigationBar = NavigationBar().apply {
        backgroundColor = rgbColorOf(51, 50, 56)
    }

    private val navigationBarContainer = VStack().apply {
        alignment = VStack.Alignment.center
        backgroundColor = rgbColorOf(38, 37, 43)
        padding.top = 24f
        padding.bottom = 24f
        addChild(navigationBar)
    }

    private val dom = DOM()

    init {
        dom.loadSite(url = url)

        navigationBar.text = url
        navigationBar.onEnter = {
            dom.loadSite(it)
        }
    }

    override fun onMeasure(measuringTape: MeasuringTape, widthConstraint: Float, heightConstraint: Float): Size {
        dom.resolveStyles(dom.rootNode.children.map { it as DOMLayoutNode })
        navigationBarContainer.measure(measuringTape, widthConstraint, heightConstraint)
        return dom.rootNode.measure(measuringTape, widthConstraint, heightConstraint)
    }

    override fun onDrawTo(canvas: Canvas) {
        dom.rootNode.drawTo(
            canvas.subRegion(
                Rect(
                    0f,
                    navigationBarContainer.frame.height,
                    dom.rootNode.frame.width,
                    dom.rootNode.frame.height
                )
            )
        )
        navigationBarContainer.drawTo(canvas.subRegion(navigationBarContainer.frame))
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
