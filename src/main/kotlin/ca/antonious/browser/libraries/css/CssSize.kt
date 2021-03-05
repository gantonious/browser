package ca.antonious.browser.libraries.css

sealed class CssSize {
    data class Pixel(val size: Int) : CssSize()
    data class Em(val size: Int) : CssSize()
    object Auto : CssSize()

    fun toFloat(): Float? {
        return when (this) {
            is Pixel -> size.toFloat()
            is Em -> size.toFloat() * 100
            is Auto -> null
        }
    }
}