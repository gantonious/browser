package ca.antonious.browser.libraries.javascript.lexer

enum class JavascriptTokenType {
    Identifier,
    Dot,
    Comma,
    OpenParentheses,
    CloseParentheses,
    Plus,
    Minus,
    Multiply,
    Divide,
    LessThan,
    GreaterThan,
    Assignment,
    Function,
    While,
    If,
    Return,
    String,
    Number,
    Boolean,
    Undefined
}

data class JavascriptToken(
    val type: JavascriptTokenType,
    val sourceInfo: SourceInfo,
    val value: String? = null
)

data class SourceInfo(val line: Int, val column: Int)