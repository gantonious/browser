package ca.antonious.browser.libraries.web.ui

import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.Color
import ca.antonious.browser.libraries.graphics.core.Font
import ca.antonious.browser.libraries.graphics.core.MeasuringTape
import ca.antonious.browser.libraries.graphics.core.Paint
import ca.antonious.browser.libraries.graphics.core.Size
import ca.antonious.browser.libraries.layout.core.InputEvent
import ca.antonious.browser.libraries.layout.core.Key
import ca.antonious.browser.libraries.layout.core.LayoutNode
import kotlin.math.min

class NavigationBar : LayoutNode() {

    var text = ""
    var onEnter: (String) -> Unit = { _ -> }

    override fun onMeasure(
        measuringTape: MeasuringTape,
        widthConstraint: Float,
        heightConstraint: Float
    ): Size {
        return Size(min(1000f, widthConstraint * 0.7f), 60f)
    }

    override fun onDrawTo(canvas: Canvas) {
        canvas.drawText(text, 32f, 16f, frame.width, Paint(Color.white), Font(name = "Arial", size = 40f))
    }

    override fun handleInputEvent(inputEvent: InputEvent) {
        when (inputEvent) {
            is InputEvent.KeyDown -> {
                val keyDownChar = inputEvent.key.char
                if (keyDownChar != null) {
                    text += keyDownChar
                } else if (inputEvent.key == Key.backspace) {
                    text = text.dropLast(1)
                } else if (inputEvent.key == Key.enter) {
                    onEnter(text)
                }
            }
        }
    }
}
