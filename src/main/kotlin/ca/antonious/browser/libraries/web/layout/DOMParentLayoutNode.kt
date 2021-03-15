package ca.antonious.browser.libraries.web.layout

import ca.antonious.browser.libraries.css.CssAlignment
import ca.antonious.browser.libraries.css.CssDisplay
import ca.antonious.browser.libraries.css.CssSize
import ca.antonious.browser.libraries.graphics.core.*
import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.layout.core.InputEvent
import ca.antonious.browser.libraries.web.DOMEvent
import ca.antonious.browser.libraries.web.ResolvedStyle
import ca.antonious.browser.libraries.web.resolveSize
import kotlin.math.max
import kotlin.math.min

class DOMParentLayoutNode(
    parent: DOMParentLayoutNode?,
    val htmlNode: HtmlElement.Node,
    val domEventHandler: (DOMEvent) -> Unit
) : DOMLayoutNode(parent, htmlNode) {

    var resolvedStyle = ResolvedStyle()
    val children = mutableListOf<DOMLayoutNode>()

    fun setChildren(children: List<DOMLayoutNode>) {
        this.children.clear()
        this.children.addAll(children)
    }

    fun resolveFontSize(measuringTape: MeasuringTape): Float {
        return when (val size = resolvedStyle.fontSize) {
            is CssSize.Pixel -> size.size * 3f
            is CssSize.Em -> (parent?.resolveFontSize(measuringTape) ?: 8f) * size.size.toFloat()
            is CssSize.Percent -> (parent?.resolveFontSize(measuringTape) ?: 8f) * size.size
            is CssSize.Auto -> 8f
        }
    }

    override fun measure(
        measuringTape: MeasuringTape,
        widthConstraint: Float,
        heightConstraint: Float
    ): Size {
        if (resolvedStyle.displayType == CssDisplay.none) {
            return Size(0f, 0f)
        }

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

        val realWidthConstraint = min(widthConstraint, styleWidth ?: widthConstraint) - explicitHorizontalMarginSize

        if (resolvedStyle.width is CssSize.Percent) {
            width = realWidthConstraint * (resolvedStyle.width as CssSize.Percent).size
        }

        height += measuringTape.resolveSize(resolvedStyle.margins.bottom) ?: 0f

        val maxWidth = realWidthConstraint

        for (child in children) {
            val childMeasureResult = child.measure(measuringTape, realWidthConstraint, heightConstraint)

            when ((child as? DOMParentLayoutNode)?.resolvedStyle?.displayType ?: CssDisplay.inlineBlock) {
                CssDisplay.block -> {
                    x = startMargin ?: 0f
                    child.frame.x = x
                    child.frame.y = y

                    height += childMeasureResult.height
                    y = height
                    childrenWidth = max(childrenWidth, (child.frame.x + childMeasureResult.width))
                }
                CssDisplay.inlineBlock -> {
                    if (childrenWidth + childMeasureResult.width > maxWidth) {
                        x = startMargin ?: 0f
                        child.frame.x = x
                        child.frame.y = y

                        height += childMeasureResult.height
                        y = height
                        childrenWidth = max(childrenWidth, (child.frame.x + childMeasureResult.width))
                    } else {
                        child.frame.x = x
                        child.frame.y = y

                        childrenWidth += childMeasureResult.width
                        x += childMeasureResult.width
                        height = max(height, (child.frame.y + childMeasureResult.height))
                    }
                }
                CssDisplay.none -> Unit
            }
        }

        val widthConstraintToFill = if (resolvedStyle.displayType == CssDisplay.inlineBlock) {
            realWidthConstraint
        } else {
            widthConstraint
        }

        if (childrenWidth < widthConstraintToFill) {
            when {
                (resolvedStyle.margins.start is CssSize.Auto && resolvedStyle.margins.end is CssSize.Auto) ||
                    (resolvedStyle.textAlignment == CssAlignment.center) -> {
                    val remainingMargin = (widthConstraintToFill - childrenWidth) / 2
                    for (child in children) {
                        child.frame.x += remainingMargin
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

    override fun handleInputEvent(inputEvent: InputEvent) {
        when (inputEvent) {
            is InputEvent.OnScrolled -> {
                frame.y -= inputEvent.dy * 100
                frame.y = min(0f, frame.y)
            }
            is InputEvent.TouchUp -> {
                if ((htmlElement as HtmlElement.Node).name == "a") {
                    domEventHandler.invoke(DOMEvent.NodeClicked(htmlElement))
                }

                for (child in children) {
                    if (child.frame.contains(inputEvent.mousePosition)) {
                        child.handleInputEvent(InputEvent.TouchUp(inputEvent.mousePosition.positionInsideOf(child.frame)))
                    }
                }
            }
            else -> {
                children.forEach { it.handleInputEvent(inputEvent) }
            }
        }
    }
}