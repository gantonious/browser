package ca.antonious.browser.libraries.layout.builtins

import ca.antonious.browser.libraries.graphics.core.*
import ca.antonious.browser.libraries.layout.core.LayoutConstraint
import ca.antonious.browser.libraries.layout.core.LayoutNode

class TextNode : LayoutNode {
    var text = ""

    override val frame = Rect.zero

    override fun measure(widthConstraint: LayoutConstraint, heightConstraint: LayoutConstraint): Size {
        return Size(width = 0f, height = 0f)
    }

    override fun drawTo(canvas: Canvas) {
        canvas.drawText(text, 0f, 0f, Paint(Color.black))
    }
}