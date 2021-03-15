package ca.antonious.browser.libraries.css

sealed class CssSize {
    data class Percent(val size: Float) : CssSize()
    data class Pixel(val size: Int) : CssSize()
    data class Em(val size: Int) : CssSize()
    object Auto : CssSize()
}
