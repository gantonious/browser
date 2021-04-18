package ca.antonious.browser.libraries.html.v2.parser

import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken

fun HtmlToken.StartTag.toElement(): HtmlElement.Node {
    return HtmlElement.Node(
        name = name,
        attributes = attributes
            .map { it.name to it.value }
            .toMap()
            .toMutableMap()
    )
}
