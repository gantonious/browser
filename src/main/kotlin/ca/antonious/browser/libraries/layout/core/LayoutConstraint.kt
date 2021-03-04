package ca.antonious.browser.libraries.layout.core

sealed class LayoutConstraint {
    object AnySize : LayoutConstraint()
    data class SpecificSize(val size: Float) : LayoutConstraint()
}