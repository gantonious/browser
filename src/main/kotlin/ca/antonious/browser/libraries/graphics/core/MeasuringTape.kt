package ca.antonious.browser.libraries.graphics.core

interface MeasuringTape {
    fun measureTextSize(text: String, desiredWidth: Float?): Size
}