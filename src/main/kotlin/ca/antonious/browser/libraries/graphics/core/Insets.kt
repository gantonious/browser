package ca.antonious.browser.libraries.graphics.core

data class Insets(var start: Float, var top: Float, var end: Float, var bottom: Float) {
    companion object {
        fun zero() = Insets(0f, 0f, 0f, 0f)
    }
}
