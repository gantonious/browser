package ca.antonious.browser.libraries.javascript.parser

class StringScanner(private val string: String) {
    var cursor: Int = 0

    val isAtEnd: Boolean
        get() = cursor >= string.length

    fun preview(): String {
        return "$string\n${" ".repeat(cursor) + "^"}"
    }

    fun moveForward() {
        cursor += 1
    }

    fun moveBack() {
        cursor -= 1
    }

    fun nextChar(): Char? {
        if (isAtEnd) return null
        return string[cursor]
    }

    fun scanUntil(char: Char, balancedAgainst: Char): String {
        var balance = 1

        if (isAtEnd) {
            return ""
        }

        val startIndex = cursor
        var nextChar = string[cursor]

        while (balance != 0 && cursor < string.length) {
            if (nextChar == balancedAgainst) {
                balance += 1
            } else if (nextChar == char) {
                balance -= 1
            }

            cursor += 1
            if (cursor < string.length) {
                nextChar = string[cursor]
            }
        }

        return if (cursor < string.length) {
            cursor += 1
            string.substring(startIndex, cursor - 1)
        } else {
            string.substring(startIndex, cursor)
        }
    }


    fun scanUntil(predicate: (Char) -> Boolean): String {
        if (isAtEnd) {
            return ""
        }

        val startIndex = cursor
        var nextChar = string[cursor]

        while (!predicate(nextChar) && cursor < string.length) {
            cursor += 1
            if (cursor < string.length) {
                nextChar = string[cursor]
            }
        }

        return if (cursor < string.length) {
            cursor += 1
            string.substring(startIndex, cursor - 1)
        } else {
            string.substring(startIndex, cursor)
        }
    }

    fun scanWhile(moveAfter: Boolean = true, predicate: (Char) -> Boolean): String {
        if (isAtEnd) {
            return ""
        }

        val startIndex = cursor
        var nextChar = string[cursor]

        while (predicate(nextChar) && cursor < string.length) {
            cursor += 1
            if (cursor < string.length) {
                nextChar = string[cursor]
            }
        }

        return if (moveAfter) {
            cursor += 1
            string.substring(startIndex, cursor - 1)
        } else {
            string.substring(startIndex, cursor)
        }
    }

    fun scanUntil(char: Char): String {
        return scanUntil { it == char }
    }

    fun moveAfterWhitespace() {
        if (isAtEnd || !string[cursor].isWhitespace()) {
            return
        }
        scanUntil { it.isWhitespace() }
    }

    fun moveAfterText(text: String) {
        if (cursor + text.length >= string.length) {
            return
        }

        if (string.substring(cursor, cursor + text.length) == text) {
            cursor += text.length
        }
    }

    fun scanUntil(text: String): String {
        if (isAtEnd) {
            return ""
        }

        val startIndex = cursor

        while (string.substring(cursor, cursor + text.length) != text && cursor + text.length < string.length) {
            cursor += 1
        }

        return if (cursor + text.length < string.length) {
            cursor += text.length + 1
            string.substring(startIndex, cursor - text.length -1)
        } else {
            string.substring(startIndex, cursor)
        }
    }
}