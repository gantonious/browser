package ca.antonious.browser.libraries.layout.builtins

import ca.antonious.browser.libraries.graphics.core.*
import ca.antonious.browser.libraries.layout.core.LayoutConstraint
import ca.antonious.browser.libraries.layout.core.LayoutNode
import kotlin.math.max

class BlockNode : LayoutNode() {
    private val children = mutableListOf<LayoutNode>()

    fun setChildren(nodes: List<LayoutNode>) {
        children.clear()
        children.addAll(nodes)
    }

    override fun measure(
        measureTape: MeasureTape,
        widthConstraint: LayoutConstraint,
        heightConstraint: LayoutConstraint
    ): Size {
        var width = 0f
        var height = 0f

        for (child in children) {
            val childMeasureResult = child.measure(measureTape, widthConstraint, heightConstraint)
            height += childMeasureResult.height
            width = max(width, childMeasureResult.width)
        }

        return Size(width = width, height = height).also {
            frame.width = it.width
            frame.height = it.height
        }
    }

    override fun drawTo(canvas: Canvas) {
        var x = 0f
        var y = 0f

        for (child in children) {
            child.drawTo(canvas.subRegion(Rect(x, y, child.frame.width, child.frame.height)))
            y += child.frame.height
        }
    }
}