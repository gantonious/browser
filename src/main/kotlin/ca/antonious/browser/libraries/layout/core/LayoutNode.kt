package ca.antonious.browser.libraries.layout.core

import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.MeasuringTape
import ca.antonious.browser.libraries.graphics.core.Rect
import ca.antonious.browser.libraries.graphics.core.Size

abstract class LayoutNode {
    var frame: Rect = Rect.zero()

    abstract fun measure(
        measuringTape: MeasuringTape,
        widthConstraint: Float,
        heightConstraint: Float
    ): Size

    abstract fun drawTo(canvas: Canvas)

    open fun handleInputEvent(inputEvent: InputEvent) = Unit
}
