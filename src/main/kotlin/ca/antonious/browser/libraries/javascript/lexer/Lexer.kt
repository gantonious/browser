package ca.antonious.browser.libraries.javascript.lexer

import kotlin.math.sin

class Lexer (private val source: String) {

    companion object {
        private val keywordTokenMap = mapOf(
            "function" to JavascriptTokenType.Function,
            "if" to JavascriptTokenType.If,
            "while" to JavascriptTokenType.While,
            "return" to JavascriptTokenType.Return,
            "true" to JavascriptTokenType.Boolean,
            "false" to JavascriptTokenType.Boolean,
            "undefined" to JavascriptTokenType.Undefined
        )

        private val singleCharTokenMap = mapOf(
            ',' to JavascriptTokenType.Comma,
            '(' to JavascriptTokenType.OpenParentheses,
            ')' to JavascriptTokenType.CloseParentheses,
            '+' to JavascriptTokenType.Plus,
            '-' to JavascriptTokenType.Minus,
            '*' to JavascriptTokenType.Multiply,
            '/' to JavascriptTokenType.Divide,
            '<' to JavascriptTokenType.LessThan,
            '>' to JavascriptTokenType.GreaterThan,
            '=' to JavascriptTokenType.Assignment
        )
    }

    private var cursor = 0
    private val tokens = mutableListOf<JavascriptToken>()

    fun lex(): List<JavascriptToken> {
        while (!isAtEnd()) {
            val currentChar = getCurrentChar()
            when {
                currentChar.isWhitespace() -> {
                    advanceCursor()
                }
                currentChar == '"' -> {
                    advanceCursor()
                    val doubleQuoteStart = cursor

                    while (!isAtEnd() && getPreviousChar() != '\\' && getCurrentChar() != '"') {
                        advanceCursor()
                    }

                    pushToken(JavascriptTokenType.String, source.substring(doubleQuoteStart, cursor))
                    advanceCursor()
                }
                currentChar.isValidIdentifierStart() ->{
                    val textStartPosition = cursor

                    do {
                        advanceCursor()
                    } while ((!isAtEnd() && getCurrentChar().let { it.isLetterOrDigit() || it == '_' }))

                    val text = source.substring(textStartPosition, cursor)

                    val matchingToken = keywordTokenMap[text]

                    if (matchingToken == null) {
                        pushToken(JavascriptTokenType.Identifier, text)
                    } else {
                        pushToken(matchingToken, text)
                    }
                }
                else -> {
                    val tokenForChar = singleCharTokenMap[currentChar]
                        ?: error("Don't know how to handle current character $currentChar")
                    pushToken(tokenForChar)

                    advanceCursor()
                }
            }
        }

        return tokens
    }

    private fun pushToken(tokenType: JavascriptTokenType, value: String? = null) {
        tokens += JavascriptToken(tokenType, SourceInfo(0, 0), value)
    }

    private fun isAtEnd(): Boolean {
        return cursor >= source.length
    }

    private fun advanceCursor() {
        cursor += 1
    }

    private fun getPreviousChar(): Char? {
        if (cursor - 1 < 0 || source.isEmpty()) {
            return null
        }

        return source[cursor - 1]
    }

    private fun getCurrentChar(): Char {
        if (isAtEnd()) {
            error("Attempted to get currentChar when at end of source.")
        }

        return source[cursor]
    }

    private fun Char.isValidIdentifierStart(): Boolean {
        return isLetterOrDigit() || this == '_' || this == '$'
    }
}