package ca.antonious.browser.libraries.graphics.core

class CanvasView(
    private val backingCanvas: Canvas,
    private val bounds: Rect
) : Canvas {

    override val globalCanvas = backingCanvas.globalCanvas
    override val size = Size(bounds.width, bounds.height)

    override fun drawRect(rect: Rect, paint: Paint) {
        backingCanvas.drawRect(Rect(rect.x + bounds.x, rect.y + bounds.y, rect.width, rect.height), paint)
    }

    override fun drawText(text: String, x: Float, y: Float, width: Float, paint: Paint, font: Font) {
        backingCanvas.drawText(text, bounds.x + x, bounds.y + y, width, paint, font)
    }

    override fun drawBitmap(bitmap: Bitmap, x: Float, y: Float) {
        backingCanvas.drawBitmap(bitmap, bounds.x + x, bounds.y + y)
    }
}

fun Canvas.subRegion(bounds: Rect): Canvas {
    return CanvasView(backingCanvas = this, bounds = bounds)
}
