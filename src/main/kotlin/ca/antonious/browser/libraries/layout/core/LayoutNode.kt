package ca.antonious.browser.libraries.layout.core

import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.Rect
import ca.antonious.browser.libraries.graphics.core.Size

interface LayoutNode {
    val frame: Rect
    fun measure(widthConstraint: LayoutConstraint, heightConstraint: LayoutConstraint): Size
    fun drawTo(canvas: Canvas)
}