package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.css.CssSize
import ca.antonious.browser.libraries.graphics.core.Color
import ca.antonious.browser.libraries.graphics.core.MeasuringTape

data class ResolvedStyle(
    val margins: CssInsets = CssInsets.zero(),
    val padding: CssInsets = CssInsets.zero(),
    val fontSize: CssSize = CssSize.Pixel(8),
    val width: CssSize = CssSize.Auto,
    val backgroundColor: Color = Color.clear
)

data class CssInsets(
    var start: CssSize,
    var end: CssSize,
    var top: CssSize,
    var bottom: CssSize
) {
    companion object {
        fun zero() = CssInsets(
            start = CssSize.Pixel(0),
            top = CssSize.Pixel(0),
            end = CssSize.Pixel(0),
            bottom = CssSize.Pixel(0)
        )
    }
}

fun MeasuringTape.resolveSize(size: CssSize): Float? {
    return when (size) {
        is CssSize.Pixel -> size.size * 2f
        is CssSize.Em -> size.size * 20f
        is CssSize.Auto -> null
    }
}