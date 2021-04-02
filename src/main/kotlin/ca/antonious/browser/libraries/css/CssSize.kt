package ca.antonious.browser.libraries.css

sealed class CssSize {
    data class Percent(val size: Float) : CssSize()
    data class Pixel(val size: Int) : CssSize()
    data class Em(val size: Int) : CssSize()
    object Auto : CssSize()
}

fun String.toCssSize(): CssSize {
    if (endsWith("em")) {
        return CssSize.Em(replace("em", "").trim().toInt())
    } else if (endsWith("px")) {
        return CssSize.Pixel(replace("px", "").trim().toInt())
    } else if (endsWith("%")) {
        return CssSize.Percent(replace("%", "").trim().toFloat() / 100f)
    } else if (toIntOrNull() != null) {
        return CssSize.Pixel(toInt())
    }

    return CssSize.Auto
}
