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

        fun lastNthCharacter(n: Int): Char? {
            if (cursor - n >= rawHtml.length && cursor - n > 0) {
                return null
            }
            return rawHtml[cursor - n]
        }

        fun peekNextCharacter(): Char? {
            if (cursor + 1 >= rawHtml.length) {
                return null
            }
            return rawHtml[cursor + 1]
        }

        fun peekNextNthCharacter(n: Int): Char? {
            if (cursor + n >= rawHtml.length) {
                return null
            }
            return rawHtml[cursor + n]
        }

        fun advanceCursor() {
            cursor += 1
        }

        fun advanceCursorBy(amount: Int) {
            cursor += amount
        }


        mainLoop@while (cursor < rawHtml.length) {
            when (val currentCharacter = rawHtml[cursor]) {
                '\n', '\r' -> {
                    advanceCursor()
                }
                '<' -> {
                    if (currentText.isNotBlank() && tagStack.isNotEmpty()) {
                        tagStack.peek().children += HtmlElement.Text(text = currentText.trim().replace("&nbsp;", "    "))
                    }
                    currentText = ""

                    advanceCursor()
                    when(rawHtml[cursor]) {
                        '!' -> {
                            when (peekNextCharacter()) {
                                '-' -> {
                                    advanceCursorBy(3)
                                    var commentText = ""
                                    while(!(currentCharacter() == '-' && peekNextCharacter() == '-' && peekNextNthCharacter(2) == '>')) {
                                        commentText += currentCharacter()
                                        advanceCursor()
                                    }
                                    advanceCursorBy(3)
                                }
                                else -> {
                                    while(currentCharacter().let { it != null && it != '\n' && it != '<'}) {
                                        advanceCursor()
                                    }
                                }
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

                            while (matchingOpening.name != tagName) {
                                if (matchingOpening.name in autoClosingTags) {
                                    tagStack.peek().children += HtmlElement.Node(name = matchingOpening.name, attributes = matchingOpening.attributes)
                                    tagStack.peek().children += matchingOpening.children
                                    matchingOpening = tagStack.pop()
                                } else {
                                    println("WARN: Couldn't find matching closing tag for '${matchingOpening.name}'")
                                    val node = HtmlElement.Node(name = matchingOpening.name, attributes = matchingOpening.attributes, children = matchingOpening.children)

                                    if (tagStack.isEmpty()) {
                                        return listOf(node)
                                    } else {
                                        tagStack.peek().children += node
                                        matchingOpening = tagStack.pop()
                                    }
                                }
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

                                            if (currentCharacter() == '=') {
                                                advanceCursor()

                                                while (currentCharacter() == ' ') {
                                                    advanceCursor()
                                                }

                                                var attributeTerminator: Char? = null

                                                if (currentCharacter() == '\'' || currentCharacter() == '"') {
                                                    attributeTerminator = currentCharacter() ?: ' '
                                                    advanceCursor()
                                                }

                                                fun parseAttributeValue(usingQuoteCharacter: Char?) {
                                                    var attributeValue = ""

                                                    if (usingQuoteCharacter == null) {
                                                        while (currentCharacter()?.let { it.isLetter() } == true) {
                                                            attributeValue += currentCharacter()
                                                            advanceCursor()
                                                        }
                                                    } else {
                                                        while (currentCharacter() != usingQuoteCharacter && lastCharacter() != '\\') {
                                                            attributeValue += currentCharacter()
                                                            advanceCursor()
                                                        }
                                                        advanceCursor()
                                                    }
                                                    attributes[attributeName] = attributeValue
                                                }

                                                parseAttributeValue(usingQuoteCharacter = attributeTerminator)
                                            } else {
                                                attributes[attributeName] = "true"
                                            }
                                        } else {
                                            advanceCursor()
                                        }
                                    }
                                }

                            }

                            when(rawHtml[cursor - 1]) {
                                '/' -> {
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