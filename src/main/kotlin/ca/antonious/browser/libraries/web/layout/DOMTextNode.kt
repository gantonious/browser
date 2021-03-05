package ca.antonious.browser.libraries.web.layout

import ca.antonious.browser.libraries.graphics.core.*
import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.layout.core.LayoutConstraint

class DOMTextNode(parent: DOMLayoutNode?, htmlElement: HtmlElement) : DOMLayoutNode(parent, htmlElement) {

    override fun measure(measuringTape: MeasuringTape, widthConstraint: LayoutConstraint, heightConstraint: LayoutConstraint): Size {
        val desiredWidth = when (widthConstraint) {
            is LayoutConstraint.SpecificSize -> widthConstraint.size
            else -> null
        }
        return measuringTape.measureTextSize(htmlElement.requireAsText().text, desiredWidth).also {
            frame.width = it.width
            frame.height = it.height
        }
    }

    override fun drawTo(canvas: Canvas) {
        canvas.drawText(htmlElement.requireAsText().text, 0f, 0f, frame.width, Paint(Color.black))
    }
}