package ca.antonious.browser.libraries.web.layout

import ca.antonious.browser.libraries.css.CssSize
import ca.antonious.browser.libraries.graphics.core.*
import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.layout.core.LayoutConstraint
import ca.antonious.browser.libraries.web.ResolvedStyle
import ca.antonious.browser.libraries.web.resolveSize
import kotlin.math.max
import kotlin.math.min

class DOMParentLayoutNode(
    parent: DOMLayoutNode?,
    htmlElement: HtmlElement
) : DOMLayoutNode(parent, htmlElement) {

    var resolvedStyle = ResolvedStyle()
    val children = mutableListOf<DOMLayoutNode>()

    fun setChildren(children: List<DOMLayoutNode>) {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun measure(
        measuringTape: MeasuringTape,
        widthConstraint: LayoutConstraint,
        heightConstraint: LayoutConstraint
    ): Size {
        val explicitTopMargin = measuringTape.resolveSize(resolvedStyle.margins.top) ?: 0f

        var width = 0f
        var height = explicitTopMargin

        var x = 0f
        var y = explicitTopMargin

        val styleWidth = measuringTape.resolveSize(resolvedStyle.width)

        val startMargin = measuringTape.resolveSize(resolvedStyle.margins.start)
        val endMargin = measuringTape.resolveSize(resolvedStyle.margins.end)

        val explicitHorizontalMarginSize = (startMargin ?: 0f) + (endMargin ?: 0f)

        val realWidthConstraint = when (widthConstraint) {
            is LayoutConstraint.SpecificSize -> {
                LayoutConstraint.SpecificSize(min(widthConstraint.size, styleWidth ?: widthConstraint.size) - explicitHorizontalMarginSize)
            }
            is LayoutConstraint.AnySize -> {
                if (styleWidth == null) {
                    LayoutConstraint.AnySize
                } else {
                    LayoutConstraint.SpecificSize(styleWidth - explicitHorizontalMarginSize)
                }
            }
        }

        height += measuringTape.resolveSize(resolvedStyle.margins.bottom) ?: 0f

        for (child in children) {
            val childMeasureResult = child.measure(measuringTape, realWidthConstraint, heightConstraint)
            child.frame.x = x + (startMargin ?: 0f)
            child.frame.y = y

            height += childMeasureResult.height
            y = height

            width = max(width, childMeasureResult.width)
        }

        when (widthConstraint) {
            is LayoutConstraint.SpecificSize -> {
                if (width < widthConstraint.size) {
                    when {
                        resolvedStyle.margins.start is CssSize.Auto && resolvedStyle.margins.end is CssSize.Auto -> {
                            val remainingMargin = (widthConstraint.size - width) / 2
                            for (child in children) {
                                child.frame.x = remainingMargin
                            }
                        }
                    }
                }
            }
        }

        return Size(width = width, height = height).also {
            frame.width = it.width
            frame.height = it.height
        }
    }

    override fun drawTo(canvas: Canvas) {
        canvas.drawRect(Rect(0f, 0f, frame.width, frame.height), Paint(color = resolvedStyle.backgroundColor))
        children.forEach {
            it.drawTo(canvas.subRegion(it.frame))
        }
    }
}