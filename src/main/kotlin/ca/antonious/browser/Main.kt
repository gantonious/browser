package ca.antonious.browser

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.viewport.ScreenViewport


class HelloApp : ApplicationAdapter() {
    lateinit var stage: Stage

    override fun create() {
        stage = Stage(ScreenViewport())

//        val generator = FreeTypeFontGenerator(Gdx.files.internal("truetypefont/Amble-Light.ttf"))
//        val parameter: FreeTypeFontGenerator.FreeTypeFontParameter = FreeTypeFontParameter()
//        parameter.size = 30
//        parameter.borderWidth = 1
//        parameter.color = Color.YELLOW
//        parameter.shadowOffsetX = 3
//        parameter.shadowOffsetY = 3
//        parameter.shadowColor = Color(0, 0.5f, 0, 0.75f)
//        val font24: BitmapFont = generator.generateFont(parameter) // font size 24 pixels

//        generator.dispose()
        val labelStyle = Label.LabelStyle()
        labelStyle.font = BitmapFont()
        labelStyle.fontColor = Color.BLACK

        val label2 = Label("Title", labelStyle)
        label2.setSize(100f, 100f)
        label2.setPosition(20f, 20f)
        stage.addActor(label2)


    }

    override fun render() {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act()
        stage.draw()
    }
}

fun main() {
    val config = LwjglApplicationConfiguration().apply {
        title = "Browser"
    }
    LwjglApplication(HelloApp(), config)
}