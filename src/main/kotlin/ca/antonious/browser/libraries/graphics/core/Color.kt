package ca.antonious.browser.libraries.graphics.core

data class Color(val r: Float, val g: Float, val b: Float, val a: Float) {
    companion object {
        val black = Color(0f, 0f, 0f, 1f)
        val white = Color(1f, 1f, 1f, 1f)
        val blue = Color(0f, 0f, 1f, 1f)
        val clear = Color(0f, 0f, 0f, 0f)
    }
}

fun String.toColor(): Color {
    val hexCode = trim().replace("#", "").toIntOrNull(16) ?: return Color.clear
    val r = (hexCode and 0xFF0000) shr  16
    val g = (hexCode and 0x00FF00) shr 8
    val b = hexCode and 0x0000FF

    return Color(r.toFloat() / 255f, g.toFloat() /255f, b.toFloat() / 255f, 1f)
}