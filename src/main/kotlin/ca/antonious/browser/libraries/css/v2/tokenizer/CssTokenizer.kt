package ca.antonious.browser.libraries.css.v2.tokenizer

class CssTokenizer(
    val source: String,
    val filename: String
) {
    private var cursor = 0

    fun consumeToken(): CssTokenType {
        consumeComments()
        val currentInputCodePoint = consumeNextInputCodePoint()

        return when {
            currentInputCodePoint == null -> CssTokenType.EndOfFile
            currentInputCodePoint.isWhitespace() -> {
                consumeAsMuchWhitespaceAsPossible()
                CssTokenType.Whitespace
            }
            currentInputCodePoint == '"' -> consumeStringToken('"')
            currentInputCodePoint == '#' -> {
                return if (nextInputCodePoint()?.isIdentCodePoint() == true || checkIfTwoCodePointsAreAValidEscape(currentInputCodePoint, nextInputCodePoint())) {
                    var hashToken = CssTokenType.HashToken(type = "", value = "")

                    if (
                        checkIfThreeCodePointsWouldStartAnIdentSequence(
                            nextInputCodePoint(),
                            nextInputCodePoint(lookAhead = 1),
                            nextInputCodePoint(lookAhead = 2)
                        )
                    ) {
                        hashToken.type = "id"
                    }

                    hashToken.value = consumeAnIdentSequence()

                    hashToken
                } else {
                    CssTokenType.Delim(value = currentInputCodePoint)
                }
            }
            currentInputCodePoint == '\'' -> consumeStringToken('\'')
            currentInputCodePoint == '(' -> CssTokenType.LeftParenthesis
            currentInputCodePoint == ')' -> CssTokenType.RightParenthesis
            currentInputCodePoint == '+' -> {
                if (checkIfInputStreamStartsWithANumber()) {
                    reconsumeCurrentInputCodePoint()
                    consumeNumericToken()
                } else {
                    CssTokenType.Delim(value = currentInputCodePoint)
                }
            }
            currentInputCodePoint == ',' -> CssTokenType.Comma
            currentInputCodePoint == '-' -> {
                if (checkIfInputStreamStartsWithANumber()) {
                    reconsumeCurrentInputCodePoint()
                    return consumeNumericToken()
                }

                if (nextInputCodePoint() == '-' && nextInputCodePoint(lookAhead = 1) == '>') {
                    return CssTokenType.CDC
                }

                if (checkIfInputStreamStartsWithAnIdentSequence()) {
                    reconsumeCurrentInputCodePoint()
                    return consumeAnIdentLikeToken()
                }

                CssTokenType.Delim(value = currentInputCodePoint)
            }
            currentInputCodePoint == '.' -> {
                if (checkIfInputStreamStartsWithANumber()) {
                    reconsumeCurrentInputCodePoint()
                    consumeNumericToken()
                } else {
                    CssTokenType.Delim(value = currentInputCodePoint)
                }
            }
            currentInputCodePoint == ':' -> CssTokenType.Colon
            currentInputCodePoint == ';' -> CssTokenType.SemiColon
            currentInputCodePoint == '<' -> {
                if (
                    nextInputCodePoint() == '!' &&
                    nextInputCodePoint(lookAhead = 1) == '-' &&
                    nextInputCodePoint(lookAhead = 2) == '-'
                ) {
                    consumeNextInputCodePoint()
                    consumeNextInputCodePoint()
                    consumeNextInputCodePoint()
                    CssTokenType.CDO
                } else {
                    CssTokenType.Delim(value = currentInputCodePoint)
                }
            }
            currentInputCodePoint == '@' -> {
                if (
                    checkIfThreeCodePointsWouldStartAnIdentSequence(
                        nextInputCodePoint(),
                        nextInputCodePoint(lookAhead = 1),
                        nextInputCodePoint(lookAhead = 2),
                    )
                ) {
                    CssTokenType.AtKeyword(value = consumeAnIdentSequence())
                } else {
                    CssTokenType.Delim(value = currentInputCodePoint)
                }
            }
            currentInputCodePoint == '[' -> CssTokenType.LeftBracket
            currentInputCodePoint == '\\' -> {
                if (checkIfInputStreamStartsWithAValidEscape()) {
                    reconsumeCurrentInputCodePoint()
                    consumeAnIdentLikeToken()
                } else {
                    raiseParseError()
                    CssTokenType.Delim(value = currentInputCodePoint)
                }
            }
            currentInputCodePoint == ']' -> CssTokenType.RightBracket
            currentInputCodePoint == '{' -> CssTokenType.LeftCurlyBracket
            currentInputCodePoint == '}' -> CssTokenType.RightCurlyBracket
            currentInputCodePoint.isDigit() -> {
                reconsumeCurrentInputCodePoint()
                consumeNumericToken()
            }
            currentInputCodePoint.isIdentCodeStartPoint() -> {
                reconsumeCurrentInputCodePoint()
                consumeAnIdentLikeToken()
            }
            else -> CssTokenType.Delim(value = currentInputCodePoint)
        }
    }

    private fun consumeAnIdentLikeToken(): CssTokenType {
        val string = consumeAnIdentSequence()

        if (string.lowercase() == "url" && nextInputCodePoint() == '(') {
            consumeNextInputCodePoint()

            while (nextInputCodePoint()?.isWhitespace() == true && nextInputCodePoint(lookAhead = 1)?.isWhitespace() == true) {
                consumeNextInputCodePoint()
            }

            return when {
                (nextInputCodePoint() == '"' || nextInputCodePoint() == '\'') ||
                (nextInputCodePoint()?.isWhitespace() == true && nextInputCodePoint(lookAhead = 1) == '"' || nextInputCodePoint(lookAhead = 1) == '\'') -> {
                    CssTokenType.Function(value = string)
                }
                else -> {
                    consumeUrlToken()
                }
            }
        }

        if (nextInputCodePoint() == '(') {
            consumeNextInputCodePoint()
            CssTokenType.Function(value = string)
        }

        return CssTokenType.Ident(value = string)
    }

    private fun consumeUrlToken(): CssTokenType {
        val urlToken = CssTokenType.Url(value = "")

        consumeAsMuchWhitespaceAsPossible()

        while (true) {
            val currentInputCodePoint = consumeNextInputCodePoint()

            when {
                currentInputCodePoint == ')' -> return urlToken
                currentInputCodePoint == null -> {
                    raiseParseError()
                    return urlToken
                }
                currentInputCodePoint.isWhitespace() -> {
                    consumeAsMuchWhitespaceAsPossible()

                    if (nextInputCodePoint() == ')') {
                        consumeNextInputCodePoint()
                        return urlToken
                    }

                    if (nextInputCodePoint() == null) {
                        raiseParseError()
                        consumeNextInputCodePoint()
                        return urlToken
                    }

                    consumeRemnantsOfABadUrl()
                    return CssTokenType.BadUrl
                }
                currentInputCodePoint == '"' ||
                currentInputCodePoint == '\'' ||
                currentInputCodePoint == '(' ||
                currentInputCodePoint.isNonPrintableCodePoint() -> {
                    raiseParseError()
                    consumeRemnantsOfABadUrl()
                    return CssTokenType.BadUrl
                }
                currentInputCodePoint == '\\' -> {
                    if (checkIfInputStreamStartsWithAValidEscape()) {
                        urlToken.value += consumeAnEscapedCodePoint()
                    } else {
                        raiseParseError()
                        consumeRemnantsOfABadUrl()
                        return CssTokenType.BadUrl
                    }
                }
                else -> urlToken.value += currentInputCodePoint
            }
        }
    }

    private fun consumeRemnantsOfABadUrl() {
        while (true) {
            val currentInputCodePoint = consumeNextInputCodePoint()

            when {
                currentInputCodePoint == ')' || currentInputCodePoint == null -> return
                checkIfInputStreamStartsWithAValidEscape() -> {
                    consumeAnEscapedCodePoint()
                }

            }
        }
    }

    private fun consumeComments() {
        if (nextInputCodePoint() == '/' && nextInputCodePoint(1) == '*') {
            consumeNextInputCodePoint()
            consumeNextInputCodePoint()

            while (!(nextInputCodePoint() == null || (nextInputCodePoint() == '*' && nextInputCodePoint(lookAhead =  1) == '/'))) {
                consumeNextInputCodePoint()
            }

            consumeNextInputCodePoint()
            consumeNextInputCodePoint()
        }
    }

    private fun consumeAsMuchWhitespaceAsPossible() {
        while (nextInputCodePoint()?.isWhitespace() == true) {
            consumeNextInputCodePoint()
        }
    }

    private fun consumeNumericToken(): CssTokenType {
        val (numberValue, numberType) = consumeANumber()

        if (
            checkIfThreeCodePointsWouldStartAnIdentSequence(
                nextInputCodePoint(),
                nextInputCodePoint(lookAhead = 1),
                nextInputCodePoint(lookAhead = 2),
            )
        ) {
            return CssTokenType.Dimension(
                value = numberValue,
                type = numberType,
                unit = consumeAnIdentSequence()
            )
        }

        if (nextInputCodePoint() == '%') {
            consumeNextInputCodePoint()

            return CssTokenType.Percent(value = numberValue)
        }

        return CssTokenType.Number(value = numberValue, type = numberType)
    }

    private fun consumeANumber(): Pair<Double, String> {
        var type = "integer"
        var repr = ""

        if (nextInputCodePoint() in setOf('+', '-')) {
            repr += consumeNextInputCodePoint()
        }

        while (nextInputCodePoint()?.isDigit() == true) {
            repr += consumeNextInputCodePoint()
        }

        if (nextInputCodePoint() == '.' && nextInputCodePoint(lookAhead = 1)?.isDigit() == true) {
            repr += consumeNextInputCodePoint()
            repr += consumeNextInputCodePoint()
            type = "number"

            while (nextInputCodePoint()?.isDigit() == true) {
                repr += consumeNextInputCodePoint()
            }
        }

        if (nextInputCodePoint()?.lowercaseChar() == 'e') {
            if (nextInputCodePoint(lookAhead = 1) in setOf('+', '-') && nextInputCodePoint(lookAhead = 2)?.isDigit() == true) {
                repr += consumeNextInputCodePoint()
                repr += consumeNextInputCodePoint()
                repr += consumeNextInputCodePoint()
                type = "number"
                while (nextInputCodePoint()?.isDigit() == true) {
                    repr += consumeNextInputCodePoint()
                }
            }

            if (nextInputCodePoint(lookAhead = 1)?.isDigit() == true) {
                repr += consumeNextInputCodePoint()
                repr += consumeNextInputCodePoint()
                type = "number"
                while (nextInputCodePoint()?.isDigit() == true) {
                    repr += consumeNextInputCodePoint()
                }
            }
        }

        return repr.toDouble() to type
    }

    private fun consumeStringToken(endingCodePoint: Char): CssTokenType {
        val token = CssTokenType.String(value = "")

        while (true) {
            when(val currentCodePoint = consumeNextInputCodePoint()) {
                endingCodePoint -> return token
                null -> {
                    raiseParseError()
                    return token
                }
                '\n' -> {
                    raiseParseError()
                    reconsumeCurrentInputCodePoint()
                    return CssTokenType.BadString
                }
                '\\' -> {
                    val nextInputCodePoint = nextInputCodePoint()

                    if (nextInputCodePoint == null) {
                        continue
                    }

                    if (nextInputCodePoint == '\n') {
                        consumeNextInputCodePoint()
                    }

                    token.value += consumeAnEscapedCodePoint()
                }
                else -> {
                    token.value += currentCodePoint
                }
            }
        }
    }

    private fun consumeAnEscapedCodePoint(): Char {
        var currentCodePoint = consumeNextInputCodePoint()

        when {
            currentCodePoint == null -> {
                raiseParseError()
                return '\uFFFD'
            }
            currentCodePoint.isHexDigit() -> {
                var hexDigits = "$currentCodePoint"

                for (i in 0 until 5) {
                    currentCodePoint = consumeNextInputCodePoint()
                    if (currentCodePoint?.isHexDigit() != true) {
                        hexDigits += currentCodePoint
                    }
                }

                if (nextInputCodePoint()?.isWhitespace() == true) {
                    consumeNextInputCodePoint()
                }

                val number = hexDigits.toInt(16)

                if (number == 0 || number in 0xD800..0xDFFF || number > 0xFFFF) {
                    return '\uFFFD'
                }

                return Char(number)
            }
            else -> return currentCodePoint
        }
    }

    private fun consumeAnIdentSequence(): String {
        var result = ""

        while (true) {
            val currentCodePoint = consumeNextInputCodePoint()
            when {
                currentCodePoint?.isIdentCodePoint() == true -> result += currentCodePoint
                checkIfTwoCodePointsAreAValidEscape(currentCodePoint, nextInputCodePoint()) -> result += consumeAnEscapedCodePoint()
                else -> {
                    reconsumeCurrentInputCodePoint()
                    return result
                }
            }
        }
    }

    private fun checkIfInputStreamStartsWithAValidEscape(): Boolean {
        return checkIfTwoCodePointsAreAValidEscape(currentInputCodePoint(), nextInputCodePoint())
    }

    private fun checkIfTwoCodePointsAreAValidEscape(firstCodePoint: Char?, secondCodePoint: Char?): Boolean {
        if (firstCodePoint != '\\') {
            return false
        }

        return secondCodePoint != '\n'
    }

    private fun checkIfInputStreamStartsWithAnIdentSequence(): Boolean {
        return checkIfThreeCodePointsWouldStartAnIdentSequence(
            currentInputCodePoint(),
            nextInputCodePoint(),
            nextInputCodePoint(lookAhead = 1)
        )
    }

    private fun checkIfThreeCodePointsWouldStartAnIdentSequence(
        firstCodePoint: Char?,
        secondCodePoint: Char?,
        thirdCodePoint: Char?
    ): Boolean {
        return when {
            firstCodePoint == '-' -> {
                (secondCodePoint?.isIdentCodeStartPoint() == true || secondCodePoint == '-') || checkIfTwoCodePointsAreAValidEscape(secondCodePoint, thirdCodePoint)
            }
            firstCodePoint?.isIdentCodeStartPoint() == true -> true
            firstCodePoint == '\\' -> checkIfTwoCodePointsAreAValidEscape(firstCodePoint, secondCodePoint)
            else -> false
        }
    }

    private fun checkIfInputStreamStartsWithANumber(): Boolean {
        return checkIfThreeCodePointsWouldStartANumber(
            currentInputCodePoint(),
            nextInputCodePoint(),
            nextInputCodePoint(lookAhead = 1)
        )
    }

    private fun checkIfThreeCodePointsWouldStartANumber(
        firstCodePoint: Char?,
        secondCodePoint: Char?,
        thirdCodePoint: Char?,
    ) : Boolean {
        return when {
            firstCodePoint == '+' || firstCodePoint == '-' -> {
                if (secondCodePoint?.isDigit() == true) {
                    true
                } else {
                    secondCodePoint == '.' && thirdCodePoint?.isDigit() == true
                }
            }
            firstCodePoint == '.' -> {
                secondCodePoint?.isDigit() == true
            }
            firstCodePoint?.isDigit() == true -> true
            else -> false
        }
    }

    private fun reconsumeCurrentInputCodePoint() {
        cursor--
    }

    private fun consumeNextInputCodePoint(): Char? {
        if (cursor < source.length) {
            return source[cursor++]
        }

        return null
    }

    private fun currentInputCodePoint(): Char? {
        if (cursor - 1 < source.length) {
            return source[cursor - 1]
        }

        return null
    }

    private fun nextInputCodePoint(lookAhead: Int = 0): Char? {
        if (cursor + lookAhead < source.length) {
            return source[cursor + lookAhead]
        }

        return null
    }

    private fun raiseParseError() {

    }
}

fun Char.isHexDigit(): Boolean {
    return isDigit() ||
            this == 'a' || this == 'A' ||
            this == 'b' || this == 'B' ||
            this == 'c' || this == 'C' ||
            this == 'd' || this == 'D' ||
            this == 'e' || this == 'E' ||
            this == 'f' || this == 'F'
}

fun Char.isNonAsciiCodePoint(): Boolean {
    return this.code >= 0x0080
}
fun Char.isIdentCodeStartPoint(): Boolean {
    return isLetter() || isNonAsciiCodePoint() || this == '\u005F'
}

fun Char.isIdentCodePoint(): Boolean {
    return isIdentCodeStartPoint() || isDigit() || this == '\u002D'
}

fun Char.isNonPrintableCodePoint(): Boolean {
    return code in 0x0000..0x0008 ||
        code == 0x000B ||
        code in 0x000E..0x001F ||
        code == 0x007F
}