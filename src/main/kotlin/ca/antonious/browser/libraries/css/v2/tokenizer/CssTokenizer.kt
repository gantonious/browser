package ca.antonious.browser.libraries.css.v2.tokenizer

import ca.antonious.browser.libraries.shared.parsing.SourceInfo
import kotlin.math.max
import kotlin.math.min

class CssTokenizer(
    val source: String,
    val filename: String
) {

    companion object {

        private val singleCharTokenMap = mapOf(
            "(" to CssTokenType.OpenParentheses,
            ")" to CssTokenType.CloseParentheses,
            "[" to CssTokenType.OpenSquareBracket,
            "]" to CssTokenType.CloseSquareBracket,
            "{" to CssTokenType.OpenCurlyBracket,
            "}" to CssTokenType.CloseCurlyBracket,
            "," to CssTokenType.Comma,
            "." to CssTokenType.Dot,
            ":" to CssTokenType.Colon,
            ";" to CssTokenType.SemiColon
        )
    }

    private var cursor = 0
    private var sourceRow = 0
    private var sourceColumnAtParse = 0
    private var sourceColumn = 0

    private val tokens = mutableListOf<CssToken>()

    fun tokenize(): List<CssToken> {
        while (!isAtEnd()) {
            sourceColumnAtParse = sourceColumn
            val currentChar = getCurrentChar()

            when {
                currentChar.isWhitespace() -> {
                    advanceCursor()

                    if (currentChar == '\n') {
                        sourceRow += 1
                        sourceColumn = 0
                    }
                }
                currentChar.isValidIdentifierStart() -> {
                    val textStartPosition = cursor

                    do {
                        advanceCursor()
                    } while ((!isAtEnd() && getCurrentChar().let { it.isLetterOrDigit() || it == '_' }))

                    val text = source.substring(textStartPosition, cursor)
                    pushToken(CssTokenType.Identifier(text))
                }
                else -> {
                    val tokenForChar = singleCharTokenMap[currentChar]
                        ?: abort("Found unexpected character '$currentChar' when expecting token")
                    pushToken(tokenForChar)

                    advanceCursor()
                }
            }
        }

        return tokens
    }

    private fun pushToken(tokenType: CssTokenType) {
        tokens += CssToken(tokenType, SourceInfo(sourceRow, sourceColumnAtParse, filename, source))
    }

    private fun dropLastToken() {
        if (tokens.isNotEmpty()) {
            tokens.removeAt(tokens.lastIndex)
        }
    }

    private fun isAtEnd(): Boolean {
        return cursor >= source.length
    }

    private fun advanceCursor() {
        sourceColumn += 1
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
            abort("Attempted to get currentChar when at end of source.")
        }

        return source[cursor]
    }

    private fun getCurrentNChars(n: Int): CharSequence? {
        if (cursor + n > source.length) {
            return null
        }

        return source.subSequence(cursor, cursor + n)
    }

    private fun peekLastChar(): Char? {
        if (cursor - 1 >= 0 && source.isNotEmpty()) {
            return source[cursor - 1]
        }

        return null
    }

    private fun peekNextChar(): Char? {
        if (cursor + 1 >= (source.length - 1)) {
            return null
        }

        return source[cursor + 1]
    }

    private fun abort(errorMessage: String): Nothing {
        val tab = " ".repeat(4)
        val previewWindow = 30
        val previewStart = max(0, cursor - previewWindow)
        val previewEnd = min(source.length, cursor + previewWindow)
        val cursorPositionInPreview = cursor - previewStart

        val fullError = "Tokenizer error, ${errorMessage.decapitalize()}:\n" +
                "${tab}at line:$sourceRow, column:$sourceColumnAtParse\n" +
                "${tab}${source.substring(previewStart, previewEnd)}\n" +
                tab + " ".repeat(cursorPositionInPreview) + "^\n" +
                "${tab}Last 5 tokens: [\n${tab.repeat(2)}" +
                tokens.takeLast(5).reversed().joinToString(separator = "\n${tab.repeat(2)}") +
                "\n$tab]"

        error(fullError)
    }

    private fun Char.isValidIdentifierStart(): Boolean {
        return isLetter()
    }
}
