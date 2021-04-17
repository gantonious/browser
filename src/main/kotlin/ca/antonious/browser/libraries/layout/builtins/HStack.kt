package ca.antonious.browser.libraries.layout.builtins

import ca.antonious.browser.libraries.graphics.core.MeasuringTape
import ca.antonious.browser.libraries.graphics.core.Size
import kotlin.math.max

class HStack : CompositeLayoutNode() {

    var alignment: Alignment = Alignment.top

    override fun onMeasure(
        measuringTape: MeasuringTape,
        widthConstraint: Float,
        heightConstraint: Float
    ): Size {
        var maxChildHeight = 0f
        var width = 0f

        for (child in children) {
            val childSize = child.measure(measuringTape, widthConstraint, heightConstraint)
            child.frame.x = width
            child.frame.y = 0f
            width += childSize.width
            maxChildHeight = max(maxChildHeight, childSize.height)
        }

        when (alignment) {
            Alignment.top -> Unit
            Alignment.center -> {
                for (child in children) {
                    child.frame.y = (maxChildHeight - child.frame.height) / 2f
                }
            }
            Alignment.bottom -> {
                for (child in children) {
                    child.frame.y = maxChildHeight - child.frame.height
                }
            }
        }

        return Size(width, maxChildHeight)
    }

    enum class Alignment {
        top,
        center,
        bottom
    }
}
