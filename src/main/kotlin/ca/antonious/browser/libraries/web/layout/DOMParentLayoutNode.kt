package ca.antonious.browser.libraries.web.layout

import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.MeasuringTape
import ca.antonious.browser.libraries.graphics.core.Size
import ca.antonious.browser.libraries.graphics.core.subRegion
import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.layout.core.LayoutConstraint
import ca.antonious.browser.libraries.web.ResolvedStyle
import kotlin.math.max
import kotlin.math.min

class DOMParentLayoutNode(
    parent: DOMLayoutNode?,
    htmlElement: HtmlElement
) : DOMLayoutNode(parent, htmlElement) {

    var resolvedStyle = ResolvedStyle()
    private val children = mutableListOf<DOMLayoutNode>()

    fun setChildren(children: List<DOMLayoutNode>) {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun measure(
        measuringTape: MeasuringTape,
        widthConstraint: LayoutConstraint,
        heightConstraint: LayoutConstraint
    ): Size {
        var width = 0f
        var height = 0f

        var x = 0f
        var y = 0f

        val realWidthConstraint = when (widthConstraint) {
            is LayoutConstraint.SpecificSize -> {
                LayoutConstraint.SpecificSize(min(widthConstraint.size, resolvedStyle.width ?: widthConstraint.size))
            }
            is LayoutConstraint.AnySize -> {
                if (resolvedStyle.width == null) {
                    LayoutConstraint.AnySize
                } else {
                    LayoutConstraint.SpecificSize(resolvedStyle.width!!)
                }
            }
        }

        for (child in children) {
            val childMeasureResult = child.measure(measuringTape, realWidthConstraint, heightConstraint)
            child.frame.x = x
            child.frame.y = y

            height += childMeasureResult.height
            y = height

            width = max(width, childMeasureResult.width)
        }

        return Size(width = width, height = height).also {
            frame.width = it.width
            frame.height = it.height
        }
    }

    override fun drawTo(canvas: Canvas) {
        children.forEach {
            it.drawTo(canvas.subRegion(it.frame))
        }
    }
}