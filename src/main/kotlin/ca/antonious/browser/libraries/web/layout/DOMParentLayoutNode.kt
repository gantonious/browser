package ca.antonious.browser.libraries.web.layout

import ca.antonious.browser.libraries.css.CssAlignment
import ca.antonious.browser.libraries.css.CssAttribute
import ca.antonious.browser.libraries.css.CssDisplay
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
        var childrenWidth = 0f
        var height = explicitTopMargin

        val styleWidth = measuringTape.resolveSize(resolvedStyle.width)
        val startMargin = measuringTape.resolveSize(resolvedStyle.margins.start)
        val endMargin = measuringTape.resolveSize(resolvedStyle.margins.end)
        val explicitHorizontalMarginSize = (startMargin ?: 0f) + (endMargin ?: 0f)

        var x = startMargin ?: 0f
        var y = explicitTopMargin

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

        if (resolvedStyle.width is CssSize.Percent) {
            when (realWidthConstraint) {
                is LayoutConstraint.SpecificSize -> {
                    width = realWidthConstraint.size * (resolvedStyle.width as CssSize.Percent).size
                }
            }
        }

        height += measuringTape.resolveSize(resolvedStyle.margins.bottom) ?: 0f

        for (child in children) {
            val childMeasureResult = child.measure(measuringTape, realWidthConstraint, heightConstraint)
            child.frame.x = x
            child.frame.y = y

            when ((child as? DOMParentLayoutNode)?.resolvedStyle?.displayType ?: CssDisplay.block) {
                CssDisplay.block -> {
                    height += childMeasureResult.height
                    y = height
                    childrenWidth = max(childrenWidth, childMeasureResult.width)
                }
                CssDisplay.inlineBlock -> {
                    childrenWidth += childMeasureResult.width
                    x += childMeasureResult.width
                    height = max(height, childMeasureResult.height)
                }
            }
        }

        when (widthConstraint) {
            is LayoutConstraint.SpecificSize -> {
                if (childrenWidth < widthConstraint.size) {
                    when {
                        (resolvedStyle.margins.start is CssSize.Auto && resolvedStyle.margins.end is CssSize.Auto) ||
                        (resolvedStyle.textAlignment == CssAlignment.center) -> {
                            val remainingMargin = (widthConstraint.size - childrenWidth) / 2
                            for (child in children) {
                                child.frame.x += remainingMargin
                            }
                        }
                    }
                }
            }
        }

        return Size(width = max(childrenWidth, width), height = height).also {
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