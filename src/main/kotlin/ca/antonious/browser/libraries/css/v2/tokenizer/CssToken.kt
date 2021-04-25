package ca.antonious.browser.libraries.css.v2.tokenizer

import ca.antonious.browser.libraries.shared.SourceInfo

sealed class CssTokenType {
    object OpenParentheses : CssTokenType()
    object CloseParentheses : CssTokenType()
    object OpenSquareBracket: CssTokenType()
    object CloseSquareBracket : CssTokenType()
    object OpenCurlyBracket: CssTokenType()
    object CloseCurlyBracket : CssTokenType()
    object Comma : CssTokenType()
    object Dot : CssTokenType()
    object Colon : CssTokenType()
    object SemiColon : CssTokenType()

    data class Identifier(val name: String) : CssTokenType()
}

data class CssToken(
    val type: CssTokenType,
    val sourceInfo: SourceInfo
)
