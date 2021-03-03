package ca.antonious.browser.libraries.html

import ca.antonious.browser.libraries.javascript.parser.StringScanner

class HtmlParser {
    fun parse(rawHtml: String): List<HtmlElement> {
        val scanner = StringScanner(rawHtml)
        val children = mutableListOf<HtmlElement>()
        scanner.moveAfterText("<!DOCTYPE html>")

        while (!scanner.isAtEnd) {
            scanner.moveAfterWhitespace()

            if (scanner.nextChar() == '<') {
                scanner.moveForward()
                val tagContent = scanner.scanUntil('>')
                val tagChildren = scanner.scanUntil("</$tagContent>")
                children += HtmlElement.Node(name = tagContent, children = parse(tagChildren))
            } else {
                children += HtmlElement.Text(text = rawHtml)
                break
            }
        }

        return children
    }
}