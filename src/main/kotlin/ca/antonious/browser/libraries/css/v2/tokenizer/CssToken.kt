package ca.antonious.browser.libraries.css.v2.tokenizer

import ca.antonious.browser.libraries.shared.parsing.SourceInfo

sealed class CssTokenType {
    object EndOfFile : CssTokenType()
    object Whitespace : CssTokenType()
    data class String(var value: kotlin.String): CssTokenType()
    object BadString: CssTokenType()
    object LeftParenthesis : CssTokenType()
    object RightParenthesis : CssTokenType()
    object LeftBracket : CssTokenType()
    object RightBracket : CssTokenType()
    object LeftCurlyBracket : CssTokenType()
    object RightCurlyBracket : CssTokenType()
    object Comma : CssTokenType()
    object CDC : CssTokenType()
    object Colon : CssTokenType()
    object SemiColon : CssTokenType()
    object CDO : CssTokenType()
    object BadUrl : CssTokenType()

    data class HashToken(var type: kotlin.String, var value: kotlin.String): CssTokenType()
    data class Delim(var value: Char) : CssTokenType()
    data class Dimension(var value: Double, var type: kotlin.String, var unit: kotlin.String) : CssTokenType()
    data class Percent(var value: Double) : CssTokenType()
    data class Number(var value: Double, var type: kotlin.String) : CssTokenType()
    data class Ident(var value: kotlin.String) : CssTokenType()
    data class Function(var value: kotlin.String) : CssTokenType()
    data class AtKeyword(var value: kotlin.String) : CssTokenType()
    data class Url(var value: kotlin.String) : CssTokenType()
}

data class CssToken(
    val type: CssTokenType,
    val sourceInfo: SourceInfo
)
