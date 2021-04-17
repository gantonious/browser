package ca.antonious.browser.libraries.layout.builtins

import ca.antonious.browser.libraries.graphics.core.MeasuringTape
import ca.antonious.browser.libraries.graphics.core.Size
import kotlin.math.max

class VStack : CompositeLayoutNode() {

    var alignment: Alignment = Alignment.start

    override fun onMeasure(
        measuringTape: MeasuringTape,
        widthConstraint: Float,
        heightConstraint: Float
    ): Size {
        var height = 0f

        for (child in children) {
            val childSize = child.measure(measuringTape, widthConstraint, heightConstraint)
            child.frame.x = 0f
            child.frame.y = height
            height += childSize.height
        }

        when (alignment) {
            Alignment.start -> Unit
            Alignment.center -> {
                for (child in children) {
                    child.frame.x = (widthConstraint - child.frame.width) / 2f
                }
            }
            Alignment.end -> {
                for (child in children) {
                    child.frame.x = widthConstraint - child.frame.width
                }
            }
        }

        return Size(widthConstraint, height)
    }

    enum class Alignment {
        start,
        center,
        end
    }
}
