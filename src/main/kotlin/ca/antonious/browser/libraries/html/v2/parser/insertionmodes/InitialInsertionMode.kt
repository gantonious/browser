package ca.antonious.browser.libraries.html.v2.parser.insertionmodes

import ca.antonious.browser.libraries.html.v2.parser.HtmlParser
import ca.antonious.browser.libraries.html.v2.parser.HtmlParserInsertionMode
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.isHtmlWhiteSpace

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
