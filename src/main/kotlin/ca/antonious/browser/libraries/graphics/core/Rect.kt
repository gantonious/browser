package ca.antonious.browser.libraries.graphics.core

data class Rect(var x: Float, var y: Float, var width: Float, var height: Float) {
    val x2 get() = x + width
    val y2 get() = y + height

    companion object {
        fun zero() = Rect(0f, 0f, 0f, 0f)
    }

    fun contains(point: Point): Boolean {
        return point.x >= x && point.y >= y && point.x <= x2 && point.y <= y2
    }

    fun rectWithin(insets: Insets): Rect {
        return copy(
            x = x + insets.start,
            y = y + insets.top,
            width = width - insets.start - insets.end,
            height = height - insets.top - insets.bottom
        )
    }
}
