package ca.antonious.browser.libraries.css

sealed class CssAttribute {
    data class Width(val size: CssSize) : CssAttribute()
    data class Margin(val size: CssSize) : CssAttribute()
}