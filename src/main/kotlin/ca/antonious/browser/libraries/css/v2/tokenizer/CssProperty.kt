package ca.antonious.browser.libraries.css.v2.tokenizer

import ca.antonious.browser.libraries.graphics.core.Color as GraphicsColor

sealed class CssProperty {
    data class Color(val color: GraphicsColor) : CssProperty()
    data class BackgroundColor(val color: GraphicsColor) : CssProperty()
    data class Width(val size: CssSize) : CssProperty()
    data class Height(val size: CssSize) : CssProperty()

    data class Margin(
        val marginTop: CssMargin,
        val marginRight: CssMargin,
        val marginBottom: CssMargin,
        val marginLeft: CssMargin
    ) : CssProperty()

    data class MarginTop(val margin: CssMargin) : CssProperty()
    data class MarginBottom(val margin: CssMargin) : CssProperty()
    data class MarginRight(val margin: CssMargin) : CssProperty()
    data class MarginLeft(val margin: CssMargin) : CssProperty()

}