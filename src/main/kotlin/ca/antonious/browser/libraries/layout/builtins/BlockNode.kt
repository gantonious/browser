package ca.antonious.browser.libraries.layout.builtins

import ca.antonious.browser.libraries.graphics.core.*
import ca.antonious.browser.libraries.layout.core.LayoutConstraint
import ca.antonious.browser.libraries.layout.core.LayoutNode
import ca.antonious.browser.libraries.web.DOMElement
import kotlin.math.max
import kotlin.math.min

class BlockNode : LayoutNode() {
    lateinit var element: DOMElement
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

        val realWidthConstraint = when (widthConstraint) {
            is LayoutConstraint.SpecificSize -> {
                LayoutConstraint.SpecificSize(min(widthConstraint.size, element.resolvedStyle.width ?: widthConstraint.size))
            }
            is LayoutConstraint.AnySize -> {
                if (element.resolvedStyle.width == null) {
                    LayoutConstraint.AnySize
                } else {
                    LayoutConstraint.SpecificSize(element.resolvedStyle.width!!)
                }
            }
        }

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
//        canvas.drawRect(frame, paint = Paint(backgroundColor))
        var y = 0f

        for (child in children) {
            child.drawTo(canvas.subRegion(Rect(0f, y, child.frame.width, child.frame.height)))
            y += child.frame.height
        }
    }
}