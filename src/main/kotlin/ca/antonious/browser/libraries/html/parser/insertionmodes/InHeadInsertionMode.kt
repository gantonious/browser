package ca.antonious.browser.libraries.html.parser.insertionmodes

import ca.antonious.browser.libraries.html.parser.HtmlParser
import ca.antonious.browser.libraries.html.parser.HtmlParserInsertionMode
import ca.antonious.browser.libraries.html.parser.toElement
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.states.ScriptDataState

object InHeadInsertionMode : HtmlParserInsertionMode {

    override fun process(token: HtmlToken, parser: HtmlParser) {
        when {
            token is HtmlToken.Character -> parser.insertCharacter(token.char)
            token is HtmlToken.Comment -> Unit
            token is HtmlToken.Doctype -> Unit
            token is HtmlToken.StartTag && token.name == "html" -> {
                InBodyInsertionMode.process(token, parser)
            }
            token is HtmlToken.StartTag && token.name == "base" ||
            token is HtmlToken.StartTag && token.name == "basefront" ||
            token is HtmlToken.StartTag && token.name == "bgsound" ||
            token is HtmlToken.StartTag && token.name == "link" -> {
                parser.insertHtmlElement(token)
                parser.popCurrentNode()
                token.acknowledgeSelfClosingIfSet()
            }
            token is HtmlToken.StartTag && token.name == "meta" -> {
                parser.insertHtmlElement(token)
                parser.popCurrentNode()
                token.acknowledgeSelfClosingIfSet()
            }
            token is HtmlToken.StartTag && token.name == "title" -> {
                parser.parseTokenUsingRCDATA(token)
            }
            token is HtmlToken.StartTag && token.name == "noframes" ||
            token is HtmlToken.StartTag &&  token.name == "style" -> {
                parser.parseTokenUsingRAWTEXT(token)
            }
            token is HtmlToken.StartTag && token.name == "script" -> {
                val adjustedInsertionLocation = parser.findAppropriatePlaceForInsertingNode()
                val element = token.toElement()
                adjustedInsertionLocation.node.children.add(element)
                parser.stackOfOpenElements.push(element)
                parser.tokenizer.switchStateTo(ScriptDataState)
                parser.setOriginalInsertionModeToCurrentInsertionMode()
                parser.switchInsertionModeTo(TextInsertionMode)
            }
            token is HtmlToken.EndTag && token.name == "head" -> {
                parser.popCurrentNode()
                parser.switchInsertionModeTo(AfterHeadInsertionMode)
            }
        }
    }
}
