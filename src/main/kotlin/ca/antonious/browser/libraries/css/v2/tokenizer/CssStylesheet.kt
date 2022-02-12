package ca.antonious.browser.libraries.css.v2.tokenizer

data class CssStylesheet(
    var location: String,
    var rules: List<CssRule>
)