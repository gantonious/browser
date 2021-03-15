package ca.antonious.browser.libraries.graphics.core

data class Point(val x: Float, val y: Float) {
    fun positionInsideOf(rect: Rect): Point {
        return Point(x = x - rect.x, y = y - rect.y)
    }
}
