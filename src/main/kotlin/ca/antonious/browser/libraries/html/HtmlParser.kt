package ca.antonious.browser.libraries.html

import ca.antonious.browser.libraries.javascript.parser.StringScanner

class HtmlParser {
    fun parse(rawHtml: String): List<HtmlElement> {
        val scanner = StringScanner(rawHtml)
        val children = mutableListOf<HtmlElement>()
        scanner.moveAfterText("<!DOCTYPE html>")

        while (!scanner.isAtEnd) {
            scanner.scanWhile(moveAfter = false) { it.isWhitespace() }

            if (scanner.isAtEnd) {
                break
            }

            if (scanner.nextChar() == '<') {
                scanner.moveForward()
                val tagName = scanner.scanUntil { it.isWhitespace() || it == '>' || it == '/' }
                children += when {
                    scanner.currentChar() == '>' -> {
                        when (tagName) {
                            "link", "br", "meta" -> {
                                // These tags are allowed to not be properly terminated
                                HtmlElement.Node(name = tagName, children = emptyList())
                            }
                            else -> {
                                val tagChildren = scanner.scanUntil("</$tagName>", balancedAgainst = "<$tagName")
                                HtmlElement.Node(name = tagName, children = parse(tagChildren))
                            }
                        }

                    }
                    scanner.currentChar() == '/' && scanner.nextChar() == '>' -> {
                        HtmlElement.Node(name = tagName, children = emptyList())
                    }
                    else -> {
                        val tagContent = scanner.scanUntil('>')

                        if (tagContent.endsWith("/")) {
                            HtmlElement.Node(name = tagName, children = emptyList())
                        } else if (tagName in setOf("link", "br", "meta", "img")) {
                            HtmlElement.Node(name = tagName, children = emptyList())
                        } else {
                            val tagChildren = scanner.scanUntil("</$tagName>", balancedAgainst = "<$tagName")
                            HtmlElement.Node(name = tagName, children = parse(tagChildren))
                        }
                    }
                }
            } else {
                children += HtmlElement.Text(text = scanner.scanWhile(moveAfter = false) { it != '<' })
            }
        }

        return children
    }
}