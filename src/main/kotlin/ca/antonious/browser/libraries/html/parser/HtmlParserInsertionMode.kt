package ca.antonious.browser.libraries.html.parser

import ca.antonious.browser.libraries.html.tokenizer.HtmlToken

interface HtmlParserInsertionMode {
    fun process(token: HtmlToken, parser: HtmlParser)
}
