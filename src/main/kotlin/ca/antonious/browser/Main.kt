package ca.antonious.browser

import ca.antonious.browser.core.WebContentRunner
import ca.antonious.browser.libraries.html.HtmlDocument
import ca.antonious.browser.libraries.html.HtmlElement
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator


data class Point(val x: Float, val y: Float)
data class RenderResult(val width: Float, val height: Float)

class HelloApp : ApplicationAdapter() {
    private var document: HtmlDocument? = null
    private lateinit var spriteBatch: SpriteBatch
    private var fontGenerator = FreeTypeFontGenerator(FileHandle("./Arial.ttf"))
    private lateinit var font: BitmapFont
    private lateinit var camera: OrthographicCamera
    private lateinit var webContentRunner: WebContentRunner


    override fun create() {
        camera = OrthographicCamera(Gdx.graphics.getWidth().toFloat(), Gdx.graphics.getHeight().toFloat())
        camera.setToOrtho(true, Gdx.graphics.getWidth().toFloat(), Gdx.graphics.getHeight().toFloat())
        spriteBatch = SpriteBatch()

        font = fontGenerator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = 48
            borderStraight = true
            flip = true
            genMipMaps = true
            minFilter = Texture.TextureFilter.Nearest
            magFilter = Texture.TextureFilter.MipMapLinearNearest
            color = Color.BLACK
        })

        webContentRunner = WebContentRunner { document ->
            this.document = document
        }
    }

    override fun render() {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update()
        spriteBatch.begin()
        spriteBatch.projectionMatrix = camera.combined
        document?.let {
            renderBlock(Point(0f, 0f), htmlElement = it.root)
        }
        spriteBatch.end()
    }

    private fun renderBlock(atPoint: Point, htmlElement: HtmlElement): RenderResult {
        when (htmlElement) {
            is HtmlElement.Text -> {
                val layout = font.draw(spriteBatch, htmlElement.text, atPoint.x, atPoint.y)
                return RenderResult(layout.width, layout.height)
            }
            is HtmlElement.Node -> {
                when (htmlElement.name.toLowerCase()) {
                    "root", "body" -> {
                        var currentPoint = Point(x = atPoint.x, y = atPoint.y + htmlElement.attributes.getOrDefault("marginTop", "0").toFloat())

                        for (child in htmlElement.children) {
                            val renderResult = renderBlock(atPoint = currentPoint, htmlElement = child)
                            currentPoint = Point(x = currentPoint.x, y = currentPoint.y + renderResult.height)
                        }

                        return RenderResult(width = 0f, height = currentPoint.y - atPoint.y)
                    }
                    "h1" -> {
                        val point = Point(x = atPoint.x, y = atPoint.y + htmlElement.attributes.getOrDefault("marginTop", "0").toFloat())
                        return renderBlock(atPoint = point, htmlElement = htmlElement.requireChildrenAsText())
                    }
                    else -> {
                        return RenderResult(0f, 0f)
                    }
                }
            }
        }
    }
}

fun main() {
    val config = LwjglApplicationConfiguration().apply {
        title = "Browser"
        useHDPI = true
    }
    LwjglApplication(HelloApp(), config)
}