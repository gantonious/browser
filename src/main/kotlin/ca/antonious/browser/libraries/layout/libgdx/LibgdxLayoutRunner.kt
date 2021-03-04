package ca.antonious.browser.libraries.layout.libgdx

import ca.antonious.browser.libraries.graphics.libgdx.LibgdxCanvas
import ca.antonious.browser.libraries.graphics.libgdx.LibgdxMeasureTape
import ca.antonious.browser.libraries.layout.core.LayoutConstraint
import ca.antonious.browser.libraries.layout.core.LayoutNode
import ca.antonious.browser.libraries.layout.core.LayoutRunner
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class LibgdxLayoutRunner : LayoutRunner {
    override fun runLayout(layoutNode: LayoutNode) {
        val config = LwjglApplicationConfiguration().apply {
            title = "Browser"
            useHDPI = true
        }

        LwjglApplication(LibgdxLayoutRunnerApplication(layoutNode), config)
    }
}

private class LibgdxLayoutRunnerApplication(val rootNode: LayoutNode) : ApplicationAdapter() {
    private lateinit var spriteBatch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var camera: OrthographicCamera
    private var fontGenerator = FreeTypeFontGenerator(FileHandle("./Arial.ttf"))
    private lateinit var font: BitmapFont

    override fun create() {
        camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.width.toFloat())
        camera.setToOrtho(true, Gdx.graphics.width.toFloat(), Gdx.graphics.width.toFloat())
        spriteBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()

        font = fontGenerator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = 30
            borderStraight = true
            flip = true
            genMipMaps = true
            minFilter = Texture.TextureFilter.Nearest
            magFilter = Texture.TextureFilter.MipMapLinearNearest
            color = Color.BLACK
        })
    }

    override fun resize(width: Int, height: Int) {
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.setToOrtho(true, width.toFloat(), height.toFloat())
    }

    override fun render() {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update()
        spriteBatch.begin()
        spriteBatch.projectionMatrix = camera.combined

        val measureTape = LibgdxMeasureTape(font)

        rootNode.measure(
            measureTape = measureTape,
            widthConstraint = LayoutConstraint.SpecificSize(camera.viewportWidth),
            heightConstraint = LayoutConstraint.SpecificSize(camera.viewportHeight)
        )

        val canvas = LibgdxCanvas(spriteBatch, shapeRenderer, font)
        rootNode.drawTo(canvas)

        spriteBatch.end()
    }
}