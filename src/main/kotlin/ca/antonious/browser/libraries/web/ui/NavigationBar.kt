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

class NavigationBar : LayoutNode() {

    var text = ""
    var onEnter: (String) -> Unit = { _ -> }

    override fun measure(
        measuringTape: MeasuringTape,
        widthConstraint: Float,
        heightConstraint: Float
    ): Size {
        return Size(widthConstraint, 60f).apply {
            frame.width = width
            frame.height = height
        }
    }

    override fun drawTo(canvas: Canvas) {
        canvas.drawRect(frame, Paint(Color.black))
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
