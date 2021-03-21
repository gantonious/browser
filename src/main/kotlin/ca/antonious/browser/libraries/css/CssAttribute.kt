package ca.antonious.browser.libraries.css

import ca.antonious.browser.libraries.graphics.core.Color as GraphicsColor

sealed class CssAttribute {
    data class Width(val size: CssSize) : CssAttribute()
    data class Height(val size: CssSize) : CssAttribute()
    data class Left(val size: CssSize) : CssAttribute()
    data class Top(val size: CssSize) : CssAttribute()
    data class Bottom(val size: CssSize) : CssAttribute()
    data class Right(val size: CssSize) : CssAttribute()
    data class MarginStart(val size: CssSize) : CssAttribute()
    data class MarginEnd(val size: CssSize) : CssAttribute()
    data class MarginTop(val size: CssSize) : CssAttribute()
    data class MarginBottom(val size: CssSize) : CssAttribute()
    data class FontSize(val size: CssSize) : CssAttribute()
    data class BackgroundColor(val color: GraphicsColor) : CssAttribute()
    data class Color(val color: GraphicsColor) : CssAttribute()
    data class TextAlignment(val alignment: CssHorizontalAlignment) : CssAttribute()
    data class VerticalAlignment(val alignment: CssVerticalAlignment) : CssAttribute()
    data class Display(val displayType: CssDisplay) : CssAttribute()
    data class Position(val positionType: CssPosition) : CssAttribute()
}

enum class CssHorizontalAlignment {
    left,
    center,
    right
}

enum class CssVerticalAlignment {
    top,
    middle,
    bottom
}
