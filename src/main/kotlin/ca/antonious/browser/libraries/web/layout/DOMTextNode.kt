package ca.antonious.browser.libraries.web.layout

import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.Font
import ca.antonious.browser.libraries.graphics.core.MeasuringTape
import ca.antonious.browser.libraries.graphics.core.Paint
import ca.antonious.browser.libraries.graphics.core.Size
import ca.antonious.browser.libraries.html.HtmlElement

class DOMTextNode(parent: DOMElementNode?, htmlElement: HtmlElement) : DOMLayoutNode(parent, htmlElement) {

    var font: Font? = null

    override fun onMeasure(measuringTape: MeasuringTape, widthConstraint: Float, heightConstraint: Float): Size {
        font = Font("", parent!!.resolveFontSize(measuringTape))

        return measuringTape.measureTextSize(htmlElement.requireAsText().text, widthConstraint, font!!)
    }

    override fun onDrawTo(canvas: Canvas) {
        val color = (parent as DOMElementNode).resolvedStyle.color
        font?.let {
            canvas.drawText(htmlElement.requireAsText().text, 0f, 0f, frame.width, Paint(color), it)
        }
    }
}
