package ca.antonious.browser.libraries.css

class CssParser {
    companion object {
        val selectorTextTerminators = setOf(
            ' ', ':', '{', '\t'
        )
    }
    fun parse(rawCss: String): List<CssRule> {
        val rules = mutableListOf<CssRule>()
        var cursor = 0
        var currentSelector: CssSelector? = null

        fun currentCharacter(): Char? {
            if (cursor >= rawCss.length) {
                return null
            }
            return rawCss[cursor]
        }

        fun lastCharacter(): Char? {
            if (cursor - 1 >= rawCss.length && cursor - 1 > 0) {
                return null
            }
            return rawCss[cursor - 1]
        }

        fun peekNextCharacter(): Char? {
            if (cursor + 1 >= rawCss.length) {
                return null
            }
            return rawCss[cursor + 1]
        }

        fun advanceCursor() {
            cursor += 1
        }


        fun parseClassSelector() {
            advanceCursor()
            var className = ""

            while (currentCharacter() !in selectorTextTerminators) {
                className += currentCharacter()
                advanceCursor()
            }

            currentSelector = CssSelector.MatchesClass(name = className)
        }

        fun parseTagSelector() {
            var tagName = ""

            while (currentCharacter() !in selectorTextTerminators) {
                tagName += currentCharacter()
                advanceCursor()
            }

            currentSelector = CssSelector.MatchesTag(tag = tagName)
        }

        fun parseSelector() {
            when (currentCharacter()) {
                '.' -> parseClassSelector()
                else -> parseTagSelector()
            }
        }

        fun parseCssAttributes() {
            if (currentSelector == null) {
                error("Attempted to parse css attributes without a selector being parsed.")
            }

            while (currentCharacter()?.isWhitespace() == true) { advanceCursor() }

            if (currentCharacter() != '{') {
                error("Expected '{' following css selector")
            }
            advanceCursor()

            val attributes = mutableListOf<CssAttribute>()
            while (currentCharacter() != '}') {
                while (currentCharacter()?.isWhitespace() == true) { advanceCursor() }

                var attributeName = ""
                while (currentCharacter()?.let { it.isLetterOrDigit() || it == '-' } == true) {
                    attributeName += currentCharacter()
                    advanceCursor()
                }

                while (currentCharacter()?.isWhitespace() == true) { advanceCursor() }

                if (currentCharacter() != ':') {
                    error("Expected ':' to follow css attribute '$attributeName'")
                }
                advanceCursor()

                var attributeValue = ""
                while (currentCharacter()?.let { it != '\n' && it != ';'} == true) {
                    attributeValue += currentCharacter()
                    advanceCursor()
                }

                when (attributeName) {
                    "width" -> {
                        attributes += CssAttribute.Width(size = CssSize.Em(30))
                    }
                }

                advanceCursor()
                while (currentCharacter()?.isWhitespace() == true) { advanceCursor() }
            }

            advanceCursor()
            rules += CssRule(selector = currentSelector!!, attributes = attributes)
        }

        while (cursor < rawCss.length) {
            when (currentCharacter()) {
                '\r', '\n', '\t' -> advanceCursor()
                ' ' -> {
                    if (currentSelector != null) {
                        while (currentCharacter() == ' ') { advanceCursor() }
                        when (currentCharacter()) {
                            '{' -> parseCssAttributes()
                            else -> error("Parent selectors not supported")
                        }
                    } else {
                        advanceCursor()
                    }
                }
                else -> parseSelector()
            }
        }

        return rules
    }
}