package ca.antonious.browser.libraries.html.v2.parser.insertionmodes

import ca.antonious.browser.libraries.html.v2.parser.HtmlParser
import ca.antonious.browser.libraries.html.v2.parser.HtmlParserInsertionMode
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.isHtmlWhiteSpace

object BeforeHeadInsertionMode : HtmlParserInsertionMode {
    private val allowedEndTags = setOf("head", "body", "html", "br")

    override fun process(token: HtmlToken, parser: HtmlParser) {
        fun onAnythingElse() {
            val element = parser.insertHtmlElement(HtmlToken.StartTag(name = "head"))
            parser.setHeadElementPointer(element)
            parser.switchInsertionModeTo(InHeadInsertionMode)
            InHeadInsertionMode.process(token, parser)
        }

        when {
            token is HtmlToken.Character && token.char.isHtmlWhiteSpace() -> Unit
            token is HtmlToken.Comment -> Unit
            token is HtmlToken.Doctype -> Unit
            token is HtmlToken.StartTag && token.name == "html" -> {
                InBodyInsertionMode.process(token, parser)
            }
            token is HtmlToken.StartTag && token.name == "head" -> {
                val element = parser.insertHtmlElement(token)
                parser.setHeadElementPointer(element)
                parser.switchInsertionModeTo(InHeadInsertionMode)
            }
            token is HtmlToken.EndTag && token.name in allowedEndTags -> onAnythingElse()
            token is HtmlToken.EndTag -> Unit
            else -> onAnythingElse()
        }
    }
}
