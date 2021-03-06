package ca.antonious.browser.libraries.layout.core

sealed class InputEvent {
    data class OnScrolled(val dy: Float) : InputEvent()
}