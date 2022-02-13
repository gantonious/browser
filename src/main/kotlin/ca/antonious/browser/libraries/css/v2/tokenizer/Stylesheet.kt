package ca.antonious.browser.libraries.css.v2.tokenizer

data class Stylesheet(
    var location: String,
    var rules: List<CssRule>
)

data class CssStylesheet(
    val styleRules: List<CssStyleRule>
)

data class CssStyleRule(
    val selectors: List<CssSelector>,
    val contents: List<StyleDeclaration>
)