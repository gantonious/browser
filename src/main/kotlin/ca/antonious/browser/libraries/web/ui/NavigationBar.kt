package ca.antonious.browser.libraries.web.ui

import ca.antonious.browser.libraries.graphics.core.*
import ca.antonious.browser.libraries.layout.core.InputEvent
import ca.antonious.browser.libraries.layout.core.Key
import ca.antonious.browser.libraries.layout.core.LayoutConstraint
import ca.antonious.browser.libraries.layout.core.LayoutNode

class NavigationBar : LayoutNode() {

    var text = ""
    var onEnter: (String) -> Unit = { _ -> }

    override fun measure(
        measuringTape: MeasuringTape,
        widthConstraint: LayoutConstraint,
        heightConstraint: LayoutConstraint
    ): Size {
        val width = when (widthConstraint) {
            is LayoutConstraint.SpecificSize -> widthConstraint.size
            is LayoutConstraint.AnySize -> 1000f
        }

        return Size(width, 60f).apply {
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