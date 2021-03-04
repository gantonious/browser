package ca.antonious.browser.libraries.graphics.core

class CanvasView(
    private val backingCanvas: Canvas,
    private val bounds: Rect
) : Canvas {

    override fun drawRect(rect: Rect, paint: Paint) {
        backingCanvas.drawRect(Rect(rect.x + bounds.x, rect.y + bounds.y, rect.width, rect.height), paint)
    }

    override fun drawText(text: String, x: Float, y: Float, paint: Paint) {
        backingCanvas.drawText(text, bounds.x  + x, bounds.y + y, paint)
    }
}

fun Canvas.subRegion(bounds: Rect): Canvas {
    return CanvasView(backingCanvas = this, bounds = bounds)
}