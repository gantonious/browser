package ca.antonious.browser.libraries.graphics.core

data class Color(val r: Float, val g: Float, val b: Float, val a: Float) {
    companion object {
        val black = Color(0f, 0f, 0f, 1f)
        val white = Color(1f, 1f, 1f, 1f)
        val blue = Color(0f, 0f, 1f, 1f)
    }
}