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

    fun currentChar(): Char? {
        if (string.isEmpty()) {
            return null
        }

        return string[cursor - 1]
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

        if (cursor < string.length) {
            cursor += 1
            return string.substring(startIndex, cursor - 1)
        } else {
            return string.substring(startIndex, cursor)
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

        if (cursor < string.length) {
            cursor += 1
            return string.substring(startIndex, cursor - 1)
        } else {
            return string.substring(startIndex, cursor)
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

        if (cursor < string.length && moveAfter) {
            cursor += 1
            return string.substring(startIndex, cursor - 1)
        } else {
            return string.substring(startIndex, cursor)
        }
    }

    fun scanAround(char: Char): Pair<String, String?> {
        if (isAtEnd) {
            return "" to null
        }

        val startIndex = cursor
        var nextChar = string[cursor]

        while (nextChar != char) {
            cursor += 1
            if (cursor < string.length) {
                nextChar = string[cursor]
            }
        }

        return if (cursor < string.length) {
            cursor += 1
            string.substring(startIndex, cursor - 1) to string.substring(cursor)
        } else {
            string.substring(startIndex, cursor) to null
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

    fun leftOfCursor(): String {
        return string.substring(0, cursor - 2)
    }

    fun rightOfCursor(): String {
        return string.substring(cursor, string.length - 1)
    }
}