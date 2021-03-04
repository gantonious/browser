package ca.antonious.browser.libraries.graphics.libgdx

import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.Paint
import ca.antonious.browser.libraries.graphics.core.Rect
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class LibgdxCanvas (
    private val spiteBatch: SpriteBatch,
    private val shapeRenderer: ShapeRenderer
) : Canvas {

    override fun drawRect(rect: Rect, paint: Paint) {
        shapeRenderer.color = Color(paint.color.r, paint.color.g, paint.color.b, paint.color.a)
        shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height)
    }

    override fun drawText(text: String, x: Float, y: Float, paint: Paint) {
        TODO("Not yet implemented")
    }
}