package ca.antonious.browser.libraries.javascript.lexer

sealed class JavascriptTokenType {
    data class Identifier(val name: kotlin.String) : JavascriptTokenType()
    object Dot : JavascriptTokenType()
    object Comma : JavascriptTokenType()
    object OpenParentheses : JavascriptTokenType()
    object CloseParentheses : JavascriptTokenType()
    object OpenCurlyBracket : JavascriptTokenType()
    object CloseCurlyBracket : JavascriptTokenType()
    object Plus : JavascriptTokenType()
    object Minus : JavascriptTokenType()
    object Multiply : JavascriptTokenType()
    object Divide : JavascriptTokenType()
    object LessThan : JavascriptTokenType()
    object GreaterThan : JavascriptTokenType()
    object Assignment : JavascriptTokenType()
    object Function : JavascriptTokenType()
    object While : JavascriptTokenType()
    object If : JavascriptTokenType()
    object Return : JavascriptTokenType()
    data class String(val value: kotlin.String) : JavascriptTokenType()
    data class Number(val value: Double) : JavascriptTokenType()
    data class Boolean(val value: kotlin.Boolean) : JavascriptTokenType()
    object Undefined : JavascriptTokenType()
}

data class JavascriptToken(
    val type: JavascriptTokenType,
    val sourceInfo: SourceInfo
)

data class SourceInfo(val line: Int, val column: Int)