package ca.antonious.browser.libraries.css

class CssParser {
    companion object {
        val selectorTextTerminators = setOf(
            ' ', ':', '{', '\t', '.'
        )
    }

    private val attributeParser = CssAttributeParser()

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

        fun tryParseStateSelector() {
            if (currentCharacter() != ':') {
                return
            }

            var stateName = ""
            advanceCursor()

            while (currentCharacter() !in selectorTextTerminators) {
                stateName += currentCharacter()
                advanceCursor()
            }

            currentSelector = CssSelector.MatchesState(selector = currentSelector!!, requiredState = stateName)
        }

        fun parseClassSelector() {
            advanceCursor()
            var className = ""

            while (currentCharacter() !in selectorTextTerminators) {
                className += currentCharacter()
                advanceCursor()
            }

            currentSelector = CssSelector.MatchesClass(name = className)
            tryParseStateSelector()
        }

        fun parseTagSelector() {
            var tagName = ""

            while (currentCharacter() !in selectorTextTerminators) {
                tagName += currentCharacter()
                advanceCursor()
            }

            currentSelector = CssSelector.MatchesTag(tag = tagName)
            tryParseStateSelector()
        }

        fun parseSelector() {
            when (currentCharacter()) {
                '.' -> parseClassSelector()
                else -> parseTagSelector()
            }
        }

        fun parseParentSelector() {
            val previousSelector =
                currentSelector ?: error("Can't process parent selector since no selector is being parsed.")
            parseSelector()
            val capturedCurrentSelector = currentSelector ?: error("Expected current selector after parsing selector.")

            currentSelector = when (previousSelector) {
                is CssSelector.MatchesParent -> {
                    previousSelector.copy(
                        parentSelectors = previousSelector.parentSelectors + listOf(
                            capturedCurrentSelector
                        )
                    )
                }
                else -> CssSelector.MatchesParent(listOf(previousSelector, capturedCurrentSelector))
            }
        }

        fun parseCssAttributes() {
            if (currentSelector == null) {
                error("Attempted to parse css attributes without a selector being parsed.")
            }

            while (currentCharacter()?.isWhitespace() == true) {
                advanceCursor()
            }

            if (currentCharacter() != '{') {
                error("Expected '{' following css selector")
            }
            advanceCursor()

            val attributes = mutableListOf<CssAttribute>()
            while (currentCharacter() != '}') {
                while (currentCharacter()?.isWhitespace() == true) {
                    advanceCursor()
                }

                var attributeName = ""
                while (currentCharacter()?.let { it.isLetterOrDigit() || it == '-' } == true) {
                    attributeName += currentCharacter()
                    advanceCursor()
                }

                while (currentCharacter()?.isWhitespace() == true) {
                    advanceCursor()
                }

                if (currentCharacter() != ':') {
                    error("Expected ':' to follow css attribute '$attributeName'")
                }
                advanceCursor()

                var attributeValue = ""
                while (currentCharacter()?.let { it != '\n' && it != ';' } == true) {
                    attributeValue += currentCharacter()
                    advanceCursor()
                }

                attributes += attributeParser.parse(attributeName, attributeValue)
                advanceCursor()
                while (currentCharacter()?.isWhitespace() == true) {
                    advanceCursor()
                }
            }

            advanceCursor()
            rules += CssRule(selector = currentSelector!!, attributes = attributes)
        }

        while (cursor < rawCss.length) {
            when (currentCharacter()) {
                '\r', '\n', '\t' -> advanceCursor()
                ' ' -> {
                    if (currentSelector != null) {
                        while (currentCharacter() == ' ') {
                            advanceCursor()
                        }
                        when (currentCharacter()) {
                            '{' -> parseCssAttributes()
                            else -> parseParentSelector()
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
