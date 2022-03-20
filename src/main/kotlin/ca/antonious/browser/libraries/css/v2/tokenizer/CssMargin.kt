package ca.antonious.browser.libraries.css.v2.tokenizer

sealed class CssMargin {
    object Auto : CssMargin()
    data class Length(val length: CssLength) : CssMargin()
}