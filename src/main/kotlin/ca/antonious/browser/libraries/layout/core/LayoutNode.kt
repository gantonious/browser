package ca.antonious.browser.libraries.layout.core

import ca.antonious.browser.libraries.graphics.core.*

open class LayoutNode {
    var backgroundColor: Color = Color.clear
    val margins: Insets = Insets.zero()
    val padding: Insets = Insets.zero()
    val frame: Rect = Rect.zero()

    var height: Float? = null
    var width: Float? = null
    var backgroundRadius: Float? = null

    fun measure(
        measuringTape: MeasuringTape,
        widthConstraint: Float,
        heightConstraint: Float
    ): Size {
        val measureResult = onMeasure(
            measuringTape,
            width ?: widthConstraint,
            height ?: heightConstraint
        )

        val width = measureResult.width + margins.start + margins.end + padding.start + padding.end
        val height = measureResult.height + margins.top + margins.bottom + padding.top + padding.bottom
        frame.width = width
        frame.height = height

        return measureResult.copy(width = width, height = height)
    }

    open fun onMeasure(
        measuringTape: MeasuringTape,
        widthConstraint: Float,
        heightConstraint: Float
    ): Size {
        return Size(0f, 0f)
    }

    fun drawTo(canvas: Canvas) {
        val boundsRect = frame.copy(x = 0f, y = 0f)
        val backgroundRect = boundsRect.rectWithin(margins)
        canvas.drawRect(backgroundRect, Paint(backgroundColor), backgroundRadius)
        val viewRect = backgroundRect.rectWithin(padding)
        onDrawTo(canvas.subRegion(viewRect))
    }

    open fun onDrawTo(canvas: Canvas) = Unit
    open fun handleInputEvent(inputEvent: InputEvent) = Unit
}
