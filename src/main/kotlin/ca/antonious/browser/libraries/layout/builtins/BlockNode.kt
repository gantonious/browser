package ca.antonious.browser.libraries.layout.builtins

import ca.antonious.browser.libraries.graphics.core.*
import ca.antonious.browser.libraries.layout.core.InputEvent
import ca.antonious.browser.libraries.layout.core.LayoutConstraint
import ca.antonious.browser.libraries.layout.core.LayoutNode
import kotlin.math.max
import kotlin.math.min

class BlockNode : LayoutNode() {
    val children = mutableListOf<LayoutNode>()

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
        var y = frame.y

        for (child in children) {
            child.drawTo(canvas.subRegion(Rect(0f, y, child.frame.width, child.frame.height)))
            y += child.frame.height
        }
    }

    override fun handleInputEvent(inputEvent: InputEvent) {
        when (inputEvent) {
            is InputEvent.OnScrolled -> {
                frame.y -= inputEvent.dy * 100
                frame.y = min(0f, frame.y)
            }
            is InputEvent.TouchUp -> {
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