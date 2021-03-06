package ca.antonious.browser.libraries.web.layout

import ca.antonious.browser.libraries.graphics.core.*
import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.layout.core.LayoutConstraint
import ca.antonious.browser.libraries.web.resolveSize

class DOMTextNode(parent: DOMParentLayoutNode?, htmlElement: HtmlElement) : DOMLayoutNode(parent, htmlElement) {

    var font: Font? = null

    override fun measure(measuringTape: MeasuringTape, widthConstraint: LayoutConstraint, heightConstraint: LayoutConstraint): Size {
        font = Font("", parent!!.resolveFontSize(measuringTape))

        val desiredWidth = when (widthConstraint) {
            is LayoutConstraint.SpecificSize -> widthConstraint.size
            else -> null
        }
        return measuringTape.measureTextSize(htmlElement.requireAsText().text, desiredWidth, font!!).also {
            frame.width = it.width
            frame.height = it.height
        }
    }

    override fun drawTo(canvas: Canvas) {
        val color = (parent as DOMParentLayoutNode).resolvedStyle.color
        canvas.drawText(htmlElement.requireAsText().text, 0f, 0f, frame.width, Paint(color), font!!)
    }
}