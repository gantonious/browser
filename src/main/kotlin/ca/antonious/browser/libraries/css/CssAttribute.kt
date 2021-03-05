package ca.antonious.browser.libraries.css

sealed class CssAttribute {
    data class Width(val size: CssSize) : CssAttribute()
    data class Margin(val size: CssSize) : CssAttribute()
    data class MarginStart(val size: CssSize) : CssAttribute()
    data class MarginEnd(val size: CssSize) : CssAttribute()
    data class MarginTop(val size: CssSize) : CssAttribute()
    data class MarginBottom(val size: CssSize) : CssAttribute()
}