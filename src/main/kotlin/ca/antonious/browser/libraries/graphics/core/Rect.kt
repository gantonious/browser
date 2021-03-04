package ca.antonious.browser.libraries.graphics.core

data class Rect(var x: Float, var y: Float, var width: Float, var height: Float) {
    companion object {
        fun zero() = Rect(0f, 0f, 0f, 0f)
    }
}