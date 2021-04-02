package ca.antonious.browser.libraries.javascript.lexer

import kotlin.math.max
import kotlin.math.min

class JavascriptLexer(
    private val source: String,
    private val sourceFilename: String
) {

    companion object {
        private val keywordTokenMap = mapOf(
            "function" to JavascriptTokenType.Function,
            "if" to JavascriptTokenType.If,
            "else" to JavascriptTokenType.Else,
            "while" to JavascriptTokenType.While,
            "for" to JavascriptTokenType.For,
            "return" to JavascriptTokenType.Return,
            "true" to JavascriptTokenType.Boolean(true),
            "false" to JavascriptTokenType.Boolean(false),
            "undefined" to JavascriptTokenType.Undefined,
            "null" to JavascriptTokenType.Null,
            "let" to JavascriptTokenType.Let,
            "const" to JavascriptTokenType.Const,
            "var" to JavascriptTokenType.Var,
            "new" to JavascriptTokenType.New,
            "do" to JavascriptTokenType.Do,
            "try" to JavascriptTokenType.Try,
            "catch" to JavascriptTokenType.Catch,
            "finally" to JavascriptTokenType.Finally,
            "throw" to JavascriptTokenType.Throw,
            "in" to JavascriptTokenType.In,
            "typeof" to JavascriptTokenType.TypeOf,
            "void" to JavascriptTokenType.Void,
            "delete" to JavascriptTokenType.Delete,
            "instanceof" to JavascriptTokenType.InstanceOf
        )

        private val singleCharTokenMap = mapOf(
            '.' to JavascriptTokenType.Dot,
            ',' to JavascriptTokenType.Comma,
            ':' to JavascriptTokenType.Colon,
            ';' to JavascriptTokenType.SemiColon,
            '?' to JavascriptTokenType.QuestionMark,
            '(' to JavascriptTokenType.OpenParentheses,
            ')' to JavascriptTokenType.CloseParentheses,
            '{' to JavascriptTokenType.OpenCurlyBracket,
            '}' to JavascriptTokenType.CloseCurlyBracket,
            '[' to JavascriptTokenType.OpenBracket,
            ']' to JavascriptTokenType.CloseBracket,
            '|' to JavascriptTokenType.Operator.Or,
            '&' to JavascriptTokenType.Operator.And,
            '~' to JavascriptTokenType.Operator.BitNot,
            '^' to JavascriptTokenType.Operator.Xor,
            '!' to JavascriptTokenType.Operator.Not,
            '%' to JavascriptTokenType.Operator.Mod,
            '+' to JavascriptTokenType.Operator.Plus,
            '-' to JavascriptTokenType.Operator.Minus,
            '*' to JavascriptTokenType.Operator.Multiply,
            '/' to JavascriptTokenType.Operator.Divide,
            '<' to JavascriptTokenType.Operator.LessThan,
            '>' to JavascriptTokenType.Operator.GreaterThan,
            '=' to JavascriptTokenType.Operator.Assignment
        )

        private val twoCharTokenMap = mapOf(
            "|=" to JavascriptTokenType.Operator.OrAssign,
            "&=" to JavascriptTokenType.Operator.AndAssign,
            "^=" to JavascriptTokenType.Operator.XorAssign,
            "%=" to JavascriptTokenType.Operator.ModAssign,
            "+=" to JavascriptTokenType.Operator.PlusAssign,
            "-=" to JavascriptTokenType.Operator.MinusAssign,
            "*=" to JavascriptTokenType.Operator.MultiplyAssign,
            "/=" to JavascriptTokenType.Operator.DivideAssign,
            ">=" to JavascriptTokenType.Operator.GreaterThanOrEqual,
            "<=" to JavascriptTokenType.Operator.LessThanOrEqual,
            "&&" to JavascriptTokenType.Operator.AndAnd,
            "||" to JavascriptTokenType.Operator.OrOr,
            "++" to JavascriptTokenType.PlusPlus,
            "--" to JavascriptTokenType.MinusMinus,
            "==" to JavascriptTokenType.Operator.Equals,
            "!=" to JavascriptTokenType.Operator.NotEquals,
            "<<" to JavascriptTokenType.Operator.LeftShift,
            ">>" to JavascriptTokenType.Operator.RightShift,
            "=>" to JavascriptTokenType.Arrow
        )

        private val threeCharTokenMap = mapOf(
            "===" to JavascriptTokenType.Operator.StrictEquals,
            "!==" to JavascriptTokenType.Operator.StrictNotEquals
        )
    }

    private var cursor = 0
    private var sourceRow = 0
    private var sourceColumnAtParse = 0
    private var sourceColumn = 0

    private val tokens = mutableListOf<JavascriptToken>()

    fun lex(): List<JavascriptToken> {
        mainLoop@ while (!isAtEnd()) {
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
                currentChar == '"' -> {
                    advanceCursor()
                    val stringStart = cursor

                    while (!isAtEnd() && getCurrentChar() != '"' || (getCurrentChar() == '"' && getPreviousChar() == '\\')) {
                        advanceCursor()
                    }

                    pushToken(JavascriptTokenType.String(source.substring(stringStart, cursor)))
                    advanceCursor()
                }
                currentChar == '\'' -> {
                    advanceCursor()
                    val stringStart = cursor

                    while (!isAtEnd() && getCurrentChar() != '\'' || (getCurrentChar() == '\'' && getPreviousChar() == '\\')) {
                        advanceCursor()
                    }

                    pushToken(JavascriptTokenType.String(source.substring(stringStart, cursor)))
                    advanceCursor()
                }
                currentChar == '/' && peekNextChar() == '/' -> {
                    while (!isAtEnd() && getCurrentChar() != '\n') {
                        advanceCursor()
                    }
                }
                currentChar == '/' && peekNextChar() == '*' -> {
                    advanceCursor()
                    advanceCursor()

                    while (!isAtEnd() && !(peekLastChar() == '*' && getCurrentChar() == '/')) {
                        advanceCursor()
                    }
                    advanceCursor()
                }
                currentChar == '/' && canEnterRegexGoal() -> {
                    advanceCursor()
                    val regexStart = cursor
                    var isEscaped = false

                    while (!isAtEnd() && getCurrentChar() != '/' || isEscaped) {
                        isEscaped = getCurrentChar() == '\\' && !isEscaped
                        advanceCursor()
                    }

                    val regexEnd = cursor
                    advanceCursor()

                    while (!isAtEnd() && getCurrentChar().isRegexFlag()) {
                        advanceCursor()
                    }

                    pushToken(
                        JavascriptTokenType.RegularExpression(
                            regex = source.substring(regexStart, regexEnd),
                            flags = source.substring(regexEnd + 1, cursor)
                        )
                    )
                }
                currentChar.isDigit() -> pushNumber()
                currentChar.isValidIdentifierStart() -> {
                    val textStartPosition = cursor

                    do {
                        advanceCursor()
                    } while ((!isAtEnd() && getCurrentChar().let { it.isLetterOrDigit() || it == '_' }))

                    val text = source.substring(textStartPosition, cursor)

                    val matchingToken = keywordTokenMap[text]

                    if (matchingToken == null) {
                        pushToken(JavascriptTokenType.Identifier(text))
                    } else {
                        pushToken(matchingToken)
                    }
                }
                else -> {
                    val tokenForNextThreeChars = threeCharTokenMap[getCurrentNChars(3)]

                    if (tokenForNextThreeChars != null) {
                        pushToken(tokenForNextThreeChars)
                        advanceCursor()
                        advanceCursor()
                        advanceCursor()
                        continue@mainLoop
                    }

                    val tokenForNextTwoChar = twoCharTokenMap[getCurrentNChars(2)]

                    if (tokenForNextTwoChar != null) {
                        pushToken(tokenForNextTwoChar)
                        advanceCursor()
                        advanceCursor()
                        continue@mainLoop
                    }

                    val tokenForChar = singleCharTokenMap[currentChar]
                        ?: abort("Found unexpected character '$currentChar' when expecting token")
                    pushToken(tokenForChar)

                    advanceCursor()
                }
            }
        }

        return tokens
    }

    private fun pushNumber() {
        val currentChar = getCurrentChar()
        val nextChar = peekNextChar()

        when {
            currentChar == '0' && (nextChar == 'x' || nextChar == 'X') -> {
                advanceCursor()
                advanceCursor()

                val digitsStart = cursor

                while (!isAtEnd() && getCurrentChar().isHexDigit()) {
                    advanceCursor()
                }

                val digitsString = source.substring(digitsStart, cursor)
                pushToken(JavascriptTokenType.Number(digitsString.toInt(16).toDouble()))
            }
            currentChar == '0' && (nextChar == 'o' || nextChar == 'O') -> {
                advanceCursor()
                advanceCursor()

                val digitsStart = cursor

                while (!isAtEnd() && getCurrentChar().isOctalDigit()) {
                    advanceCursor()
                }

                val digitsString = source.substring(digitsStart, cursor)
                pushToken(JavascriptTokenType.Number(digitsString.toInt(8).toDouble()))
            }
            currentChar == '0' && (nextChar == 'b' || nextChar == 'B') -> {
                advanceCursor()
                advanceCursor()

                val digitsStart = cursor

                while (!isAtEnd() && getCurrentChar().isBinaryDigit()) {
                    advanceCursor()
                }

                val digitsString = source.substring(digitsStart, cursor)
                pushToken(JavascriptTokenType.Number(digitsString.toInt(2).toDouble()))
            }
            else -> {
                var decimalFound = false

                if (peekLastChar() == '.') {
                    dropLastToken()
                    cursor -= 1
                }

                val digitsStart = cursor

                digitLoop@ while (!isAtEnd()) {
                    val currentDigit = getCurrentChar()
                    when {
                        !decimalFound && currentDigit == '.' -> decimalFound = true
                        !currentDigit.isDigit() -> break@digitLoop
                    }
                    advanceCursor()
                }

                val digitsString = source.substring(digitsStart, cursor)
                pushToken(JavascriptTokenType.Number(digitsString.toDouble()))
            }
        }
    }

    private fun pushToken(tokenType: JavascriptTokenType) {
        tokens += JavascriptToken(tokenType, SourceInfo(sourceRow, sourceColumnAtParse, sourceFilename))
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

    private fun canEnterRegexGoal(): Boolean {
        val lastToken = tokens.lastOrNull() ?: return true

        return when (lastToken.type) {
            is JavascriptTokenType.Identifier,
            is JavascriptTokenType.String,
            is JavascriptTokenType.Boolean,
            is JavascriptTokenType.Number,
            is JavascriptTokenType.Undefined,
            is JavascriptTokenType.PlusPlus,
            is JavascriptTokenType.MinusMinus,
            is JavascriptTokenType.CloseParentheses,
            is JavascriptTokenType.CloseCurlyBracket,
            is JavascriptTokenType.CloseBracket -> false
            else -> true
        }
    }

    private fun Char.isValidIdentifierStart(): Boolean {
        return isLetter() || this == '_' || this == '$'
    }

    private fun Char.isHexDigit(): Boolean {
        return isDigit() ||
            this == 'a' || this == 'A' ||
            this == 'b' || this == 'B' ||
            this == 'c' || this == 'C' ||
            this == 'd' || this == 'D' ||
            this == 'e' || this == 'E' ||
            this == 'f' || this == 'F'
    }

    private fun Char.isOctalDigit(): Boolean {
        return this == '0' || this == '1' ||
            this == '2' || this == '3' ||
            this == '4' || this == '5' ||
            this == '6' || this == '7'
    }

    private fun Char.isBinaryDigit(): Boolean {
        return this == '0' || this == '1'
    }

    private fun Char.isRegexFlag(): Boolean {
        return this == 'g' || this == 'i' ||
            this == 'm' || this == 's' ||
            this == 'u' || this == 'y'
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
                "\n${tab}]"

        error(fullError)
    }
}
