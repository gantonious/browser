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

    fun currentChar(): Char? {
        if (string.isNotEmpty() && cursor > 0) {
            return string[cursor - 1]
        }
        return null
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

    fun scanUntilEnd(): String {
        if (string.isEmpty() || isAtEnd) {
            return ""
        }
        return string.substring(cursor, string.length)
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

    fun scanUntil(text: String, balancedAgainst: String? = null): String {
        if (isAtEnd) {
            return ""
        }

        var balance = 1
        val startIndex = cursor

        while (balance != 0 && cursor + text.length < string.length) {
            if (string.substring(cursor, cursor + text.length) == text) {
                balance -= 1
            } else if (balancedAgainst != null && string.substring(cursor, cursor + balancedAgainst.length) == balancedAgainst) {
                balance += 1
            }
            if (balance != 0) {
                cursor += 1
            }
        }

        return if (cursor + text.length < string.length) {
            cursor += text.length + 1
            string.substring(startIndex, cursor - text.length -1)
        } else {
            string.substring(startIndex, cursor).also {
                cursor = string.length
            }
        }
    }
}