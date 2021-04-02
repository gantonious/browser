package ca.antonious.browser.libraries.graphics.libgdx

import ca.antonious.browser.libraries.graphics.core.*

class LibgdxCanvas(override val size: Size) : Canvas {
    val drawCalls = mutableListOf<LibgdxDrawCall>()

    override val globalCanvas = this

    override fun drawRect(rect: Rect, paint: Paint) {
        drawCalls += LibgdxDrawCall.DrawRect(rect, paint)
    }

    override fun drawText(text: String, x: Float, y: Float, width: Float, paint: Paint, font: Font) {
        drawCalls += LibgdxDrawCall.DrawText(text, x, y, width, paint, font)
    }

    override fun drawBitmap(bitmap: Bitmap, x: Float, y: Float) {
        drawCalls += LibgdxDrawCall.DrawBitmap(bitmap, x, y)
    }
}

sealed class LibgdxDrawCall {
    data class DrawRect(
        val rect: Rect,
        val paint: Paint
    ) : LibgdxDrawCall()

    data class DrawText(
        val text: String,
        val x: Float,
        val y: Float,
        val width: Float,
        val paint: Paint,
        val font: Font
    ) : LibgdxDrawCall()

    data class DrawBitmap(
        val bitmap: Bitmap,
        val x: Float,
        val y: Float
    ) : LibgdxDrawCall()
}
