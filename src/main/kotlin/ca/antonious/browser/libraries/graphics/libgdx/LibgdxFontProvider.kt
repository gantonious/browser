package ca.antonious.browser.libraries.graphics.libgdx

import ca.antonious.browser.libraries.graphics.core.Font
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

class LibgdxFontProvider {
    private val fontGenerator = FreeTypeFontGenerator(FileHandle("./Arial.ttf"))
    private val fontCache = mutableMapOf<Font, BitmapFont>()

    fun getFont(font: Font): BitmapFont {
        return fontCache[font] ?: kotlin.run {
            fontGenerator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = font.size.toInt()
                borderStraight = true
                flip = true
                genMipMaps = true
                minFilter = Texture.TextureFilter.Nearest
                magFilter = Texture.TextureFilter.MipMapLinearNearest
                color = Color.BLACK
            })
        }.also {
            fontCache[font] = it
        }
    }
}