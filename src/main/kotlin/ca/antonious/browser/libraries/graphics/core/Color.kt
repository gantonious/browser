package ca.antonious.browser.libraries.graphics.core

data class Color(val r: Float, val g: Float, val b: Float, val a: Float = 1f) {
    companion object {
        val black = Color(0f, 0f, 0f, 1f)
        val white = Color(1f, 1f, 1f, 1f)
        val clear = Color(0f, 0f, 0f, 0f)
        val red = Color(1f, 0f, 0f, 1f)
        val green = Color(0f, 1f, 0f, 1f)
        val blue = Color(0f, 0f, 1f, 1f)
    }
}

fun String.toColor(): Color {
    return when (this.trim()) {
        "white" -> Color.white
        "red" -> Color.red
        "blue" -> Color.blue
        "green" -> Color.green
        else -> {
            val hexCode = trim().replace("#", "").toIntOrNull(16) ?: return Color.clear
            val r = (hexCode and 0xFF0000) shr 16
            val g = (hexCode and 0x00FF00) shr 8
            val b = hexCode and 0x0000FF

            Color(r.toFloat() / 255f, g.toFloat() / 255f, b.toFloat() / 255f, 1f)
        }
    }
}

fun Int.toColor(): Color {
    val r = (this and 0xFF0000) shr 16
    val g = (this and 0x00FF00) shr 8
    val b = this and 0x0000FF

    return Color(r.toFloat() / 255f, g.toFloat() / 255f, b.toFloat() / 255f, 1f)
}

fun rgbColorOf(r: Int, g: Int, b: Int): Color {
    return Color(r / 256f, g / 256f, b / 256f, 1f)
}
