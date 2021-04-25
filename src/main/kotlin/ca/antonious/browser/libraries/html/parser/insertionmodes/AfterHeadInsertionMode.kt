package ca.antonious.browser.libraries.html.parser.insertionmodes

import ca.antonious.browser.libraries.html.parser.HtmlParser
import ca.antonious.browser.libraries.html.parser.HtmlParserInsertionMode
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.isHtmlWhiteSpace

object AfterHeadInsertionMode : HtmlParserInsertionMode {
    private val anythingElseEndTags = setOf("body", "html", "br")

    override fun process(token: HtmlToken, parser: HtmlParser) {
        fun onAnythingElse() {
            parser.insertHtmlElement(HtmlToken.StartTag(name = "body"))
            parser.switchInsertionModeTo(InBodyInsertionMode)
            InBodyInsertionMode.process(token, parser)
        }

        when {
            token is HtmlToken.Character && token.char.isHtmlWhiteSpace() -> parser.insertCharacter(token.char)
            token is HtmlToken.Comment -> Unit
            token is HtmlToken.Doctype -> Unit
            token is HtmlToken.StartTag && token.name == "html" -> {
                InBodyInsertionMode.process(token, parser)
            }
            token is HtmlToken.StartTag && token.name == "body" -> {
                parser.insertHtmlElement(token)
                parser.switchInsertionModeTo(InBodyInsertionMode)
            }
            token is HtmlToken.EndTag && token.name in anythingElseEndTags -> onAnythingElse()
            token is HtmlToken.StartTag && token.name == "head" || token is HtmlToken.EndTag -> Unit
            else -> onAnythingElse()
        }
    }
}
