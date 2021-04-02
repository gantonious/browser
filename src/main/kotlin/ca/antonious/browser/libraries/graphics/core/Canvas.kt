package ca.antonious.browser.libraries.graphics.core

interface Canvas {
    val globalCanvas: Canvas
    val size: Size

    fun drawRect(rect: Rect, paint: Paint)
    fun drawText(text: String, x: Float, y: Float, width: Float, paint: Paint, font: Font)
}
