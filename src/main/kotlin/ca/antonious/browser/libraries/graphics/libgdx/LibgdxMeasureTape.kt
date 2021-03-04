package ca.antonious.browser.libraries.graphics.libgdx

import ca.antonious.browser.libraries.graphics.core.MeasureTape
import ca.antonious.browser.libraries.graphics.core.Size
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout

class LibgdxMeasureTape(private val font: BitmapFont) : MeasureTape {

    private val glyphLayout = GlyphLayout()

    override fun measureTextSize(text: String): Size {
        glyphLayout.setText(font, text)
        return Size(width = glyphLayout.width, height = glyphLayout.height)
    }
}