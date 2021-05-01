package ca.antonious.browser.libraries.layout.libgdx

import ca.antonious.browser.libraries.graphics.core.Point
import ca.antonious.browser.libraries.graphics.core.Size
import ca.antonious.browser.libraries.graphics.libgdx.LibgdxCanvas
import ca.antonious.browser.libraries.graphics.libgdx.LibgdxDrawCall
import ca.antonious.browser.libraries.graphics.libgdx.LibgdxFontProvider
import ca.antonious.browser.libraries.graphics.libgdx.LibgdxMeasuringTape
import ca.antonious.browser.libraries.layout.core.InputEvent
import ca.antonious.browser.libraries.layout.core.LayoutNode
import ca.antonious.browser.libraries.layout.core.LayoutRunner
import ca.antonious.browser.libraries.layout.core.ApplicationExecutors
import ca.antonious.browser.libraries.shared.concurrency.RunnableQueueExecutor
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import java.util.concurrent.Executor

class LibgdxLayoutRunner : LayoutRunner {

    override val mainThreadExecutor: Executor
        = RunnableQueueExecutor()

    init {
        ApplicationExecutors.mainThreadExecutor = mainThreadExecutor
    }

    override fun runLayout(layoutNode: LayoutNode) {
        val config = LwjglApplicationConfiguration().apply {
            title = "Browser"
            useHDPI = true
        }

        LwjglApplication(LibgdxLayoutRunnerApplication(layoutNode, mainThreadExecutor as RunnableQueueExecutor), config)
    }
}

private class LibgdxLayoutRunnerApplication(
    val rootNode: LayoutNode,
    private val executor: RunnableQueueExecutor
) : ApplicationAdapter(), InputProcessor {

    private lateinit var spriteBatch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var camera: OrthographicCamera
    private val fontProvider = LibgdxFontProvider()

    private val inputEventsToProcess = mutableListOf<InputEvent>()
    private var textureCache = mutableMapOf<Int, Texture>()

    override fun create() {
        Gdx.input.inputProcessor = this
        camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.width.toFloat())
        camera.setToOrtho(true, Gdx.graphics.width.toFloat(), Gdx.graphics.width.toFloat())
        spriteBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        shapeRenderer.setAutoShapeType(true)
    }

    override fun resize(width: Int, height: Int) {
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.setToOrtho(true, width.toFloat(), height.toFloat())
    }

    override fun render() {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        camera.update()
        spriteBatch.projectionMatrix = camera.combined
        shapeRenderer.projectionMatrix = camera.combined

        val measureTape = LibgdxMeasuringTape(fontProvider)

        executor.runQueue()

        for (inputEvent in inputEventsToProcess) {
            rootNode.handleInputEvent(inputEvent)
        }

        inputEventsToProcess.clear()

        rootNode.measure(
            measuringTape = measureTape,
            widthConstraint = camera.viewportWidth,
            heightConstraint = camera.viewportHeight
        )

        val canvas = LibgdxCanvas(Size(camera.viewportWidth, camera.viewportHeight))
        rootNode.drawTo(canvas)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        canvas.drawCalls.filterIsInstance<LibgdxDrawCall.DrawRect>().forEach { drawRectCall ->
            val paint = drawRectCall.paint
            val rect = drawRectCall.rect
            shapeRenderer.color = Color(paint.color.r, paint.color.g, paint.color.b, paint.color.a)

            if (drawRectCall.cornerRadius == null) {
                shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height)
            } else {
                val radius = drawRectCall.cornerRadius
                val radius2 = radius * 2
                // center, top, bottom, start, end
                shapeRenderer.rect(rect.x + radius, rect.y + radius, rect.width - radius2, rect.height - radius2)
                shapeRenderer.rect(rect.x + radius, rect.y, rect.width - radius2, radius)
                shapeRenderer.rect(rect.x + radius, rect.y + rect.height - radius, rect.width - radius2, radius)
                shapeRenderer.rect(rect.x, rect.y + radius, radius, rect.height - radius2)
                shapeRenderer.rect(rect.x + rect.width - radius, rect.y + radius, radius, rect.height - radius2)

                // top start, top end, bottom end, bottom start
                shapeRenderer.arc(rect.x + radius, rect.y + radius, radius, 180f, 90f)
                shapeRenderer.arc(rect.x + rect.width - radius, rect.y + radius, radius, 270f, 90f)
                shapeRenderer.arc(rect.x + rect.width - radius, rect.y + rect.height - radius, radius, 0f, 90f)
                shapeRenderer.arc(rect.x + radius, rect.y + rect.height - radius, radius, 90f, 90f)
            }
        }
        shapeRenderer.end()

        spriteBatch.begin()
        canvas.drawCalls.filterIsInstance<LibgdxDrawCall.DrawText>().forEach { drawTextCall ->
            val font = fontProvider.getFont(drawTextCall.font)
            val color = drawTextCall.paint.color
            font.color = Color(color.r, color.g, color.b, color.a)
            font.draw(
                spriteBatch,
                drawTextCall.text,
                drawTextCall.x,
                drawTextCall.y,
                drawTextCall.width,
                Align.left,
                true
            )
        }

        canvas.drawCalls.filterIsInstance<LibgdxDrawCall.DrawBitmap>().forEach { drawBitmapCall ->
            try {
                val texture = textureCache[drawBitmapCall.bitmap.hashCode()] ?: kotlin.run {
                    val pixmap = Pixmap(drawBitmapCall.bitmap.bytes, 0, drawBitmapCall.bitmap.height * drawBitmapCall.bitmap.width)
                    val texture = Texture(pixmap)
                    textureCache[drawBitmapCall.bitmap.hashCode()] = texture
                    texture
                }

                spriteBatch.draw(
                    texture,
                    drawBitmapCall.x,
                    drawBitmapCall.y,
                    drawBitmapCall.width,
                    drawBitmapCall.height,
                    0,
                    0,
                    drawBitmapCall.bitmap.width,
                    drawBitmapCall.bitmap.height,
                    false,
                    true
                )
            } catch (ex: Exception) {
            }
        }
        spriteBatch.end()
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        inputEventsToProcess += InputEvent.TouchUp(Point(screenX.toFloat(), screenY.toFloat()))
        return true
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return true
    }

    override fun keyTyped(character: Char): Boolean {
        return true
    }

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        inputEventsToProcess += InputEvent.OnScrolled(dy = amountY)
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        inputEventsToProcess += InputEvent.KeyDown(key = keycode.key)
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return true
    }
}
