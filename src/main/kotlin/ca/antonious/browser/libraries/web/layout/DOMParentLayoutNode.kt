package ca.antonious.browser.libraries.web.layout

import ca.antonious.browser.libraries.css.CssHorizontalAlignment
import ca.antonious.browser.libraries.css.CssDisplay
import ca.antonious.browser.libraries.css.CssSize
import ca.antonious.browser.libraries.css.CssVerticalAlignment
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
    var marginInsets = Insets.zero()
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

        var explicitWidth: Float?
        var explicitHeight: Float?

        explicitWidth = measuringTape.resolveSize(resolvedStyle.width)
        explicitHeight = measuringTape.resolveSize(resolvedStyle.height)

        if (resolvedStyle.width is CssSize.Percent) {
            explicitWidth = widthConstraint * (resolvedStyle.width as CssSize.Percent).size
        }

        if (resolvedStyle.height is CssSize.Percent) {
            explicitHeight = heightConstraint * (resolvedStyle.height as CssSize.Percent).size
        }

        val startMargin = measuringTape.resolveSize(resolvedStyle.margins.start) ?: 0f
        val endMargin = measuringTape.resolveSize(resolvedStyle.margins.end) ?: 0f
        val explicitHorizontalMarginSize = startMargin + endMargin
        val realWidthConstraint = min(widthConstraint, explicitWidth ?: widthConstraint)

        val topMargin = measuringTape.resolveSize(resolvedStyle.margins.top) ?: 0f
        val bottomMargin = measuringTape.resolveSize(resolvedStyle.margins.bottom) ?: 0f
        val explicitVerticalMarginSize = topMargin + bottomMargin
        val realHeightConstraint = min(heightConstraint, explicitHeight ?: heightConstraint)

        marginInsets.start = startMargin
        marginInsets.end = endMargin
        marginInsets.top = topMargin
        marginInsets.bottom = bottomMargin

        var childrenWidth = 0f
        var childrenHeight = 0f
        var rowWidth = 0f
        var x = 0f
        var y = 0f

        for (child in children) {
            val childMeasureResult = child.measure(measuringTape, realWidthConstraint, realHeightConstraint)

            when ((child as? DOMParentLayoutNode)?.resolvedStyle?.displayType ?: CssDisplay.inlineBlock) {
                CssDisplay.block -> {
                    x = 0f
                    child.frame.x = x
                    child.frame.y = y

                    childrenHeight += childMeasureResult.height
                    y += childMeasureResult.height
                    rowWidth = max(rowWidth, (child.frame.x + childMeasureResult.width))
                    childrenWidth = max(childrenWidth, rowWidth)
                }
                CssDisplay.inlineBlock -> {
                    if (rowWidth + childMeasureResult.width > realWidthConstraint) {
                        x = 0f
                        y = childrenHeight
                        child.frame.x = x
                        child.frame.y = y

                        rowWidth = childMeasureResult.width
                        childrenWidth = max(childrenWidth, rowWidth)
                        x += childMeasureResult.width
                        childrenHeight += childMeasureResult.height
                    } else {
                        child.frame.x = x
                        child.frame.y = y

                        rowWidth += childMeasureResult.width
                        childrenWidth = max(childrenWidth, rowWidth)
                        x += childMeasureResult.width
                        childrenHeight = max(childrenHeight, (child.frame.y + childMeasureResult.height))
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

        val heightConstraintToFill = if (resolvedStyle.displayType == CssDisplay.block) {
            realHeightConstraint
        } else {
            heightConstraint
        }

        if (childrenWidth < widthConstraintToFill) {
            when {
                (resolvedStyle.margins.start is CssSize.Auto && resolvedStyle.margins.end is CssSize.Auto) ||
                    (resolvedStyle.textAlignment == CssHorizontalAlignment.center) -> {
                    val remainingMargin = (widthConstraintToFill - childrenWidth) / 2
                    for (child in children) {
                        child.frame.x += remainingMargin
                    }
                }
            }
        }

        if (childrenHeight < heightConstraintToFill) {
            when {
                (resolvedStyle.margins.top is CssSize.Auto && resolvedStyle.margins.bottom is CssSize.Auto) ||
                    (resolvedStyle.verticalAlignment == CssVerticalAlignment.middle) -> {
                    val remainingMargin = (heightConstraintToFill - childrenHeight) / 2
                    for (child in children) {
                        child.frame.y += remainingMargin
                    }
                }
            }
        }

        return Size(
            width = (explicitWidth ?: childrenWidth) + explicitHorizontalMarginSize,
            height = (explicitHeight ?: childrenHeight) + explicitVerticalMarginSize
        ).also {
            frame.width = it.width
            frame.height = it.height
        }
    }

    override fun drawTo(canvas: Canvas) {
        val drawRect = Rect(
            marginInsets.start,
            marginInsets.top,
            frame.width - marginInsets.start - marginInsets.end,
            frame.height - marginInsets.top - marginInsets.bottom
        )
        canvas.drawRect(
            drawRect,
            Paint(color = resolvedStyle.backgroundColor)
        )
        children.forEach {
            it.drawTo(
                canvas.subRegion(it.frame.copy(
                    x = it.frame.x + marginInsets.start,
                    y = it.frame.y + marginInsets.top
                ))
            )
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
