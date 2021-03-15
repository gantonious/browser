package ca.antonious.browser.libraries.web.layout

import ca.antonious.browser.libraries.graphics.core.*
import ca.antonious.browser.libraries.html.HtmlElement

class DOMTextNode(parent: DOMParentLayoutNode?, htmlElement: HtmlElement) : DOMLayoutNode(parent, htmlElement) {

    var font: Font? = null

    override fun measure(measuringTape: MeasuringTape, widthConstraint: Float, heightConstraint: Float): Size {
        font = Font("", parent!!.resolveFontSize(measuringTape))

        return measuringTape.measureTextSize(htmlElement.requireAsText().text, widthConstraint, font!!).also {
            frame.width = it.width
            frame.height = it.height
        }
    }

    override fun drawTo(canvas: Canvas) {
        val color = (parent as DOMParentLayoutNode).resolvedStyle.color
        font?.let {
            canvas.drawText(htmlElement.requireAsText().text, 0f, 0f, frame.width, Paint(color), it)
        }
    }
}