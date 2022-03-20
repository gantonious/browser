package ca.antonious.browser.libraries.css.v2.tokenizer

import ca.antonious.browser.libraries.graphics.core.Color
import ca.antonious.browser.libraries.graphics.core.rgbColorOf

fun convertNamedColorToColor(name: String): Color? {
    return when (name.lowercase()) {
        "aliceblue" -> rgbColorOf(240, 248, 255)
        "red" -> rgbColorOf(255, 0, 0)
        else -> null
    }
}