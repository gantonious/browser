package ca.antonious.browser.libraries.web.layout

import ca.antonious.browser.libraries.css.CssDisplay
import ca.antonious.browser.libraries.css.CssHorizontalAlignment
import ca.antonious.browser.libraries.css.CssPosition
import ca.antonious.browser.libraries.css.CssSize
import ca.antonious.browser.libraries.css.CssVerticalAlignment
import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.Insets
import ca.antonious.browser.libraries.graphics.core.MeasuringTape
import ca.antonious.browser.libraries.graphics.core.Paint
import ca.antonious.browser.libraries.graphics.core.Rect
import ca.antonious.browser.libraries.graphics.core.Size
import ca.antonious.browser.libraries.graphics.core.subRegion
import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.layout.core.InputEvent
import ca.antonious.browser.libraries.web.DOMEvent
import ca.antonious.browser.libraries.web.ResolvedStyle
import ca.antonious.browser.libraries.web.resolveSize
import kotlin.math.max
import kotlin.math.min

class DOMElementNode(
    parent: DOMElementNode?,
    val htmlNode: HtmlElement.Node,
    val domEventHandler: (DOMEvent) -> Unit
) : DOMLayoutNode(parent, htmlNode) {

    val id: String?
        get() = attributes["id"]

    val tagName: String = htmlNode.name

    val classNames: List<String>
        get() = (attributes["class"] ?: "").split(" ")

    val attributes = htmlNode.attributes.toMutableMap()

    var isHovered = false
    var resolvedStyle = ResolvedStyle()
    var marginInsets = Insets.zero()
    val children = mutableListOf<DOMLayoutNode>()

    fun setAttribute(name: String, value: String) {
        attributes[name] = value
    }

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

    override fun onMeasure(
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
            if ((child as? DOMElementNode)?.resolvedStyle?.positionType == CssPosition.absolute) {
                continue
            }

            when ((child as? DOMElementNode)?.resolvedStyle?.displayType ?: CssDisplay.inlineBlock) {
                CssDisplay.block -> {
                    val childMeasureResult = child.measure(measuringTape, realWidthConstraint, realHeightConstraint)

                    x = 0f
                    y = childrenHeight
                    child.frame.x = x
                    child.frame.y = y

                    childrenHeight += childMeasureResult.height
                    rowWidth = childMeasureResult.width
                    childrenWidth = max(childrenWidth, rowWidth)
                }
                CssDisplay.inlineBlock -> {
                    var childMeasureResult = child.measure(measuringTape, realWidthConstraint - rowWidth, realHeightConstraint)

                    if (rowWidth + childMeasureResult.width > realWidthConstraint) {
                        childMeasureResult = child.measure(measuringTape, realWidthConstraint, realHeightConstraint)
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

        if (resolvedStyle.positionType != CssPosition.absolute) {
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
        }

        val calculatedWidth = (explicitWidth ?: childrenWidth) + explicitHorizontalMarginSize
        val calculatedHeight = (explicitHeight ?: childrenHeight) + explicitVerticalMarginSize

        // Now that we know our actual size we can position absolute elements relative to our bounds
        for (child in children) {
            if ((child as? DOMElementNode)?.resolvedStyle?.positionType == CssPosition.absolute) {
                val childNode = child as DOMElementNode
                val left = measuringTape.resolveSize(childNode.resolvedStyle.left)
                val right = measuringTape.resolveSize(childNode.resolvedStyle.right)
                val top = measuringTape.resolveSize(childNode.resolvedStyle.top)
                val bottom = measuringTape.resolveSize(childNode.resolvedStyle.bottom)

                val childWidthConstraint = calculatedWidth - (left ?: 0f) - (right ?: 0f)
                val childHeightConstraint = calculatedHeight - (top ?: 0f) - (right ?: 0f)
                val childMeasureResult = child.measure(measuringTape, childWidthConstraint, childHeightConstraint)

                child.frame.x = when {
                    left != null -> left
                    right != null -> calculatedWidth - childMeasureResult.width
                    else -> 0f
                }

                child.frame.y = when {
                    top != null -> top
                    bottom != null -> calculatedHeight - childMeasureResult.height
                    else -> 0f
                }

                if (left != null && right != null) {
                    child.frame.width = childWidthConstraint
                }

                if (top != null && bottom != null) {
                    child.frame.height = childHeightConstraint
                }
            }
        }

        return Size(width = calculatedWidth, height = calculatedHeight)
    }

    override fun onDrawTo(canvas: Canvas) {
        val drawRect = Rect(
            marginInsets.start,
            marginInsets.top,
            frame.width - marginInsets.start - marginInsets.end,
            frame.height - marginInsets.top - marginInsets.bottom
        )

        if (htmlNode.name == "body") {
            canvas.globalCanvas.drawRect(
                canvas.globalCanvas.size.toRect(),
                Paint(color = resolvedStyle.backgroundColor)
            )
        } else {
            canvas.drawRect(
                drawRect,
                Paint(color = resolvedStyle.backgroundColor)
            )
        }

        children.forEach {
            it.drawTo(
                canvas.subRegion(
                    it.frame.copy(
                        x = it.frame.x + marginInsets.start,
                        y = it.frame.y + marginInsets.top
                    )
                )
            )
        }

        isHovered = false
    }

    override fun handleInputEvent(inputEvent: InputEvent) {
        when (inputEvent) {
            is InputEvent.OnScrolled -> {
                frame.y -= inputEvent.dy * 100
                frame.y = min(0f, frame.y)
            }
            is InputEvent.TouchUp -> {
                domEventHandler.invoke(DOMEvent.NodeClicked(htmlNode))

                for (child in children) {
                    if (child.frame.contains(inputEvent.mousePosition)) {
                        child.handleInputEvent(InputEvent.TouchUp(inputEvent.mousePosition.positionInsideOf(child.frame)))
                    }
                }
            }
            is InputEvent.PointerMove -> {
                isHovered = true
                for (child in children) {
                    if (child.frame.contains(inputEvent.position)) {
                        child.handleInputEvent(InputEvent.PointerMove(inputEvent.position.positionInsideOf(child.frame)))
                    }
                }
            }
            else -> {
                children.forEach { it.handleInputEvent(inputEvent) }
            }
        }
    }

    fun getElementsByClassName(className: String): List<DOMElementNode> {
        return children.flatMap {
            if (it is DOMElementNode) {
                if (it.classNames.contains(className)) {
                    listOf(it) + it.getElementsByClassName(className)
                } else {
                    it.getElementsByClassName(className)
                }
            } else {
                emptyList()
            }
        }
    }

    fun getElementsByTagName(tagName: String): List<DOMElementNode> {
        return children.flatMap {
            if (it is DOMElementNode) {
                if (it.tagName == tagName) {
                    listOf(it) + it.getElementsByTagName(tagName)
                } else {
                    it.getElementsByTagName(tagName)
                }
            } else {
                emptyList()
            }
        }
    }

    fun getElementsWithId(id: String): DOMElementNode? {
        for (child in children) {
            if (child is DOMElementNode) {
                if (child.id == id) {
                    return child
                } else {
                    child.getElementsWithId(id)?.let { return it }
                }
            }
        }

        return null
    }
}

private fun Size.toRect(): Rect {
    return Rect(0f, 0f, width, height)
}
