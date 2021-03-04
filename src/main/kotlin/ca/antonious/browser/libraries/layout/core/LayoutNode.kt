package ca.antonious.browser.libraries.layout.core

import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.MeasureTape
import ca.antonious.browser.libraries.graphics.core.Rect
import ca.antonious.browser.libraries.graphics.core.Size

abstract class LayoutNode {
    var frame: Rect = Rect.zero

    abstract fun measure(
        measureTape: MeasureTape,
        widthConstraint: LayoutConstraint,
        heightConstraint: LayoutConstraint
    ): Size

    abstract fun drawTo(canvas: Canvas)
}