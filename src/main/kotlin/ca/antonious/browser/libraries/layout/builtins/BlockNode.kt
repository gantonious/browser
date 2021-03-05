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
        measuringTape: MeasuringTape,
        widthConstraint: LayoutConstraint,
        heightConstraint: LayoutConstraint
    ): Size {
        var width = 0f
        var height = 0f

        for (child in children) {
            val childMeasureResult = child.measure(measuringTape, widthConstraint, heightConstraint)
            height += childMeasureResult.height
            width = max(width, childMeasureResult.width)
        }

        return Size(width = width, height = height).also {
            frame.width = it.width
            frame.height = it.height
        }
    }

    override fun drawTo(canvas: Canvas) {
        var y = 0f

        for (child in children) {
            child.drawTo(canvas.subRegion(Rect(0f, y, child.frame.width, child.frame.height)))
            y += child.frame.height
        }
    }
}