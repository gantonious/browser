package ca.antonious.browser.libraries.html.v2.parser

import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken

interface HtmlParserInsertionMode {
    fun process(token: HtmlToken, parser: HtmlParser)
}
