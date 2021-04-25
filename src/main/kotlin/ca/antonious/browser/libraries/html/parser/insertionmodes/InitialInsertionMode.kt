package ca.antonious.browser.libraries.html.parser.insertionmodes

import ca.antonious.browser.libraries.html.parser.HtmlParser
import ca.antonious.browser.libraries.html.parser.HtmlParserInsertionMode
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.isHtmlWhiteSpace

object InitialInsertionMode : HtmlParserInsertionMode {
    override fun process(token: HtmlToken, parser: HtmlParser) {
        return when {
            token is HtmlToken.Character && token.char.isHtmlWhiteSpace() -> Unit
            token is HtmlToken.Comment -> Unit
            token is HtmlToken.Doctype -> Unit
            else -> {
                parser.switchInsertionModeTo(BeforeHtmlInsertionMode)
                BeforeHtmlInsertionMode.process(token, parser)
            }
        }
    }
}
