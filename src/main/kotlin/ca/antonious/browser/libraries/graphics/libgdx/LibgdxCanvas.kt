package ca.antonious.browser.libraries.graphics.libgdx

import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.Paint
import ca.antonious.browser.libraries.graphics.core.Rect
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class LibgdxCanvas : Canvas {
    val drawCalls = mutableListOf<LibgdxDrawCall>()

    override fun drawRect(rect: Rect, paint: Paint) {
        drawCalls += LibgdxDrawCall.DrawRect(rect, paint)
    }

    override fun drawText(text: String, x: Float, y: Float, width: Float, paint: Paint) {
        drawCalls += LibgdxDrawCall.DrawText(text, x, y, width, paint)
    }
}

sealed class LibgdxDrawCall {
    data class DrawRect(val rect: Rect, val paint: Paint) : LibgdxDrawCall()
    data class DrawText(val text: String, val x: Float, val y: Float, val width: Float, val paint: Paint) : LibgdxDrawCall()
}