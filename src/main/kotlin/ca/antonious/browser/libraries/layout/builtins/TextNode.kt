package ca.antonious.browser.libraries.layout.builtins

import ca.antonious.browser.libraries.graphics.core.*
import ca.antonious.browser.libraries.layout.core.LayoutConstraint
import ca.antonious.browser.libraries.layout.core.LayoutNode

class TextNode : LayoutNode() {
    var text = ""

    override fun measure(measuringTape: MeasuringTape, widthConstraint: LayoutConstraint, heightConstraint: LayoutConstraint): Size {
        val desiredWidth = when (widthConstraint) {
            is LayoutConstraint.SpecificSize -> widthConstraint.size
            else -> null
        }
        return measuringTape.measureTextSize(text, desiredWidth).also {
            frame.width = it.width
            frame.height = it.height
        }
    }

    override fun drawTo(canvas: Canvas) {
        canvas.drawText(text, 0f, 0f, frame.width, Paint(Color.black))
    }
}