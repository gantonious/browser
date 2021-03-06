package ca.antonious.browser.libraries.html.parser.insertionmodes

import ca.antonious.browser.libraries.html.parser.HtmlParser
import ca.antonious.browser.libraries.html.parser.HtmlParserInsertionMode
import ca.antonious.browser.libraries.html.parser.toElement
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken

object BeforeHtmlInsertionMode : HtmlParserInsertionMode {
    override fun process(token: HtmlToken, parser: HtmlParser) {
        when {
            token is HtmlToken.StartTag && token.name == "html" -> {
                parser.stackOfOpenElements.push(token.toElement())
                parser.switchInsertionModeTo(BeforeHeadInsertionMode)
            }
            else -> {
                val element = HtmlToken.StartTag(name = "html").toElement()
                parser.stackOfOpenElements.push(element)
                parser.switchInsertionModeTo(BeforeHeadInsertionMode)
                BeforeHeadInsertionMode.process(token, parser)
            }
        }
    }
}
