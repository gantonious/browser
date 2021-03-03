package ca.antonious.browser.libraries.html

import java.util.*

data class TagParsingScope(
    val name: String,
    val attributes: Map<String, String> = emptyMap(),
    val children: MutableList<HtmlElement> = mutableListOf()
)

class HtmlParser {
    companion object {
        val autoClosingTags = setOf(
            "img",
            "a",
            "link",
            "meta",
            "br",
            "input"
        )
    }
    fun parse(rawHtml: String): List<HtmlElement> {
        var cursor = 0
        var tagStack = Stack<TagParsingScope>()
        var currentText = ""

        fun currentCharacter(): Char? {
            if (cursor >= rawHtml.length) {
                return null
            }
            return rawHtml[cursor]
        }

        fun lastCharacter(): Char? {
            if (cursor - 1 >= rawHtml.length && cursor - 1 > 0) {
                return null
            }
            return rawHtml[cursor - 1]
        }

        fun peekNextCharacter(): Char? {
            if (cursor + 1 >= rawHtml.length) {
                return null
            }
            return rawHtml[cursor + 1]
        }

        fun advanceCursor() {
            cursor += 1
        }

        mainLoop@while (cursor < rawHtml.length) {
            when (val currentCharacter = rawHtml[cursor]) {
                '\n', '\r' -> {
                    advanceCursor()
                }
                '<' -> {
                    if (currentText.isNotEmpty()) {
                        tagStack.peek().children += HtmlElement.Text(text = currentText)
                        currentText = ""
                    }

                    advanceCursor()
                    when(rawHtml[cursor]) {
                        '!' -> {
                            while(currentCharacter().let { it != null && it != '\n' }) {
                                advanceCursor()
                            }
                        }
                        '/' -> {
                            advanceCursor()
                            var tagName = ""
                            while (currentCharacter()?.isLetterOrDigit() == true) {
                                tagName += currentCharacter()
                                advanceCursor()
                            }

                            if (currentCharacter() != '>') {
                                error("Expected tag '$tagName' to be terminated.")
                            }

                            advanceCursor()

                            var matchingOpening = tagStack.pop()

                            while (matchingOpening.name != tagName && matchingOpening.name in autoClosingTags) {
                                tagStack.peek().children += HtmlElement.Node(name = matchingOpening.name, attributes = matchingOpening.attributes, children = matchingOpening.children)
                                matchingOpening = tagStack.pop()
                            }

                            if (matchingOpening.name != tagName) {
                                error("Couldn't find matching opening tag for '$tagName'")
                            }

                            val parsedNode = HtmlElement.Node(name = matchingOpening.name, attributes = matchingOpening.attributes, children = matchingOpening.children)

                            if (tagStack.isEmpty()) {
                                return listOf(parsedNode)
                            } else {
                                tagStack.peek().children += parsedNode
                            }
                        }
                        else -> {
                            var tagName = ""
                            var attributes = mutableMapOf<String, String>()

                            while (currentCharacter()?.isLetterOrDigit() == true) {
                                tagName += currentCharacter()
                                advanceCursor()
                            }

                            while (currentCharacter().let { it != null && it != '>' }) {
                                when (currentCharacter()) {
                                    ' ' -> advanceCursor()
                                    else -> {
                                        if (currentCharacter()?.isLetter() == true) {
                                            var attributeName = ""

                                            while (currentCharacter()?.let { it.isLetterOrDigit() || it == '-' || it == '_'} == true) {
                                                attributeName += currentCharacter()
                                                advanceCursor()
                                            }

                                            while (currentCharacter() == ' ') {
                                                advanceCursor()
                                            }

                                            if (currentCharacter() != '=') {
                                                error("Expected '=' following attribute name '$attributeName'.")
                                            }

                                            advanceCursor()

                                            while (currentCharacter() == ' ') {
                                                advanceCursor()
                                            }

                                            if (currentCharacter() != '\'' && currentCharacter() != '"') {
                                                error("Expected quote to start attribute")
                                            }

                                            advanceCursor()

                                            fun parseAttributeValue(usingQuoteCharacter: Char) {
                                                var attributeValue = ""

                                                while (currentCharacter() != usingQuoteCharacter && lastCharacter() != '\\') {
                                                    attributeValue += currentCharacter()
                                                    advanceCursor()
                                                }

                                                advanceCursor()
                                                attributes[attributeName] = attributeValue
                                            }

                                            parseAttributeValue(usingQuoteCharacter = lastCharacter()!!)
                                        } else {
                                            advanceCursor()
                                        }
                                    }
                                }

                            }

                            when(rawHtml[cursor - 1]) {
                                '/' -> {
                                    advanceCursor()
                                    advanceCursor()
                                    tagStack.peek().children += HtmlElement.Node(name = tagName, attributes = attributes)
                                }
                                else -> {
                                    advanceCursor()
                                    tagStack.push(TagParsingScope(name = tagName, attributes = attributes))
                                }
                            }
                        }
                    }
                }
                else -> {
                    currentText += currentCharacter
                    advanceCursor()
                }
            }
        }

        error("")
    }
}