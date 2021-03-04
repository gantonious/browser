package ca.antonious.browser.libraries.layout.builtins

import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.Rect
import ca.antonious.browser.libraries.graphics.core.Size
import ca.antonious.browser.libraries.graphics.core.subRegion
import ca.antonious.browser.libraries.layout.core.LayoutConstraint
import ca.antonious.browser.libraries.layout.core.LayoutNode

class BlockNode : LayoutNode {
    override var frame = Rect.zero
    val children = mutableListOf<LayoutNode>()

    override fun measure(widthConstraint: LayoutConstraint, heightConstraint: LayoutConstraint): Size {
        var height = 0f

        for (child in children) {
            val childMeasureResult = child.measure(widthConstraint, heightConstraint)
            height += childMeasureResult.height
        }

        return Size(width = 0f, height = height).also {
            frame.width = it.width
            frame.height = it.height
        }
    }

    override fun drawTo(canvas: Canvas) {
        var x = 0f
        var y = 0f

        for (child in children) {
            child.drawTo(canvas.subRegion(Rect(x, y, child.frame.width, child.frame.height)))
            x += child.frame.width
            y += child.frame.height
        }
    }
}