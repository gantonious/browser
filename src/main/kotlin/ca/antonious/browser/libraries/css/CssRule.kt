package ca.antonious.browser.libraries.css

data class CssRule(
    val selector: CssSelector,
    val attributes: Map<String, String>
)