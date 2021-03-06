package ca.antonious.browser.libraries.css

import ca.antonious.browser.libraries.graphics.core.Color as GraphicsColor

sealed class CssAttribute {
    data class Width(val size: CssSize) : CssAttribute()
    data class MarginStart(val size: CssSize) : CssAttribute()
    data class MarginEnd(val size: CssSize) : CssAttribute()
    data class MarginTop(val size: CssSize) : CssAttribute()
    data class MarginBottom(val size: CssSize) : CssAttribute()
    data class FontSize(val size: CssSize) : CssAttribute()
    data class BackgroundColor(val color: GraphicsColor) : CssAttribute()
    data class Color(val color: GraphicsColor) : CssAttribute()
}