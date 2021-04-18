package ca.antonious.browser.libraries.html.v2.parser.insertionmodes

import ca.antonious.browser.libraries.html.v2.parser.HtmlParser
import ca.antonious.browser.libraries.html.v2.parser.HtmlParserInsertionMode
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.isHtmlWhiteSpace

object InBodyInsertionMode : HtmlParserInsertionMode {
    private val startTagsToProcessInHead = setOf("base", "basefront", "bgsound", "link", "meta", "noframes", "script", "template", "title")

    override fun process(token: HtmlToken, parser: HtmlParser) {
        when {
            token is HtmlToken.Character && token.char.isHtmlWhiteSpace() -> {
                parser.reconstructTheActiveFormattingElements()
                parser.insertCharacter(token.char)
            }
            token is HtmlToken.Character -> {
                parser.reconstructTheActiveFormattingElements()
                parser.insertCharacter(token.char)
                parser.setFramesetOkFlagToNotOk()
            }
            token is HtmlToken.Comment -> {
                parser.insertComment(token)
            }
            token is HtmlToken.Doctype -> Unit
            token is HtmlToken.StartTag && token.name == "html" -> {
                token.attributes.forEach {
                    parser.stackOfOpenElements.first().attributes[it.name] = it.value
                }
            }
            token is HtmlToken.StartTag && token.name in startTagsToProcessInHead ||
            token is HtmlToken.EndTag && token.name == "template" -> {
                InHeadInsertionMode.process(token, parser)
            }
            token is HtmlToken.StartTag && token.name == "body" -> {
                if (
                    parser.stackOfOpenElements.size == 1 ||
                    parser.stackOfOpenElements[1].name != "body" ||
                    parser.hasTemplateOnStackOfOpenElements()
                ) {
                    return
                } else {
                    parser.setFramesetOkFlagToNotOk()
                    val secondOpenElement = parser.stackOfOpenElements[1]
                    token.attributes.forEach { (name, value) ->
                        if (name !in secondOpenElement.attributes) {
                            secondOpenElement.attributes[name] = value
                        }
                    }
                }
            }
            token is HtmlToken.StartTag && token.name == "frameset" -> {
                TODO("Support <frameset> in in-body insertion mode")
            }
            token is HtmlToken.EndOfFile -> {
                parser.stopParsing()
            }
            token is HtmlToken.EndTag && token.name == "body" -> {
                parser.switchInsertionModeTo(AfterBodyInsertionMode)
            }
            token is HtmlToken.EndTag && token.name == "html" -> {
                parser.switchInsertionModeTo(AfterBodyInsertionMode)
                AfterBodyInsertionMode.process(token, parser)
            }
            // TODO handle non ordinary tags
            token is HtmlToken.StartTag -> {
                parser.reconstructTheActiveFormattingElements()
                parser.insertHtmlElement(token)
            }
            token is HtmlToken.EndTag -> {
                for (node in parser.stackOfOpenElements.reversed()) {
                    if (node.name == token.name) {
                        parser.generateImpliedEndTags(exceptFor = node.name)
                        if (node != parser.currentNode) {
                            // parser error
                        }
                        while (parser.stackOfOpenElements.isNotEmpty()) {
                            val poppedNode = parser.popCurrentNode()
                            if (poppedNode == node) {
                                break
                            }
                        }

                        break
                    } else {
                        // check if node is special category
                    }
                }
            }
        }
    }
}
