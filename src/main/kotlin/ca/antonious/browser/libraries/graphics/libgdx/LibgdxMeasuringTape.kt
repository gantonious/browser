package ca.antonious.browser.libraries.graphics.libgdx

import ca.antonious.browser.libraries.graphics.core.Font
import ca.antonious.browser.libraries.graphics.core.MeasuringTape
import ca.antonious.browser.libraries.graphics.core.Size
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.utils.Align

class LibgdxMeasuringTape(private val fontProvider: LibgdxFontProvider) : MeasuringTape {

    private val glyphLayout = GlyphLayout()

    override fun measureTextSize(text: String, desiredWidth: Float?, font: Font): Size {
        val bitmapFont = fontProvider.getFont(font)
        return if (desiredWidth != null) {
            glyphLayout.setText(bitmapFont, text, Color.BLACK, desiredWidth, Align.left, true)
            Size(width = glyphLayout.width, height = glyphLayout.height)
        } else {
            glyphLayout.setText(bitmapFont, text)
            Size(width = glyphLayout.width, height = glyphLayout.height)
        }
    }
}
