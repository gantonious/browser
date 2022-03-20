package ca.antonious.browser.libraries.css.v2.tokenizer

sealed class CssSize {
    object Auto : CssSize()
    object MinContent : CssSize()
    object MaxContent : CssSize()
    data class Length(val length: CssLength) : CssSize()
    data class Percentage(val percent: CssTokenType.Percent) : CssSize()
}