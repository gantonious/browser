package ca.antonious.browser.libraries.html.v2.parser.insertionmodes

import ca.antonious.browser.libraries.html.v2.parser.HtmlParser
import ca.antonious.browser.libraries.html.v2.parser.HtmlParserInsertionMode
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.isHtmlWhiteSpace

object AfterBodyInsertionMode : HtmlParserInsertionMode {
    override fun process(token: HtmlToken, parser: HtmlParser) {
        when {
            token is HtmlToken.Character && token.char.isHtmlWhiteSpace() -> {
                InBodyInsertionMode.process(token, parser)
            }
            token is HtmlToken.Comment -> Unit
            token is HtmlToken.Doctype -> Unit
            token is HtmlToken.StartTag && token.name == "html" -> {
                InBodyInsertionMode.process(token, parser)
            }
            token is HtmlToken.EndTag && token.name == "html" -> {
                parser.switchInsertionModeTo(AfterAfterBodyInsertionMode)
            }
            token is HtmlToken.EndOfFile -> parser.stopParsing()
            else -> {
                parser.switchInsertionModeTo(InBodyInsertionMode)
                InBodyInsertionMode.process(token, parser)
            }
        }
    }
}
