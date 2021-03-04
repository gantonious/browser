package ca.antonious.browser.libraries.css

sealed class CssSelector {
    data class MatchesClass(val name: String) : CssSelector()
    data class MatchesTag(val tag: String) : CssSelector()
}