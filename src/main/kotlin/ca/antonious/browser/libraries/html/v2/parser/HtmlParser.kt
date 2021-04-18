package ca.antonious.browser.libraries.html.v2.parser

import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.html.v2.parser.insertionmodes.InitialInsertionMode
import ca.antonious.browser.libraries.html.v2.parser.insertionmodes.TextInsertionMode
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.states.RCDATAState
import java.util.*

class HtmlParser(source: String) {
    val tokenizer = HtmlTokenizer(source)
    private var _originalInsertionMode: HtmlParserInsertionMode? = null
    private var insertionMode: HtmlParserInsertionMode = InitialInsertionMode
    var stackOfOpenElements = Stack<HtmlElement.Node>()

    val currentNode: HtmlElement.Node
        get() = stackOfOpenElements.peek()

    val originalInsertionMode: HtmlParserInsertionMode
        get() = _originalInsertionMode!!

    fun parse(): HtmlElement.Node {
        var nextToken = tokenizer.nextToken()

        while (true) {
            insertionMode.process(nextToken, this)
            if (nextToken is HtmlToken.EndOfFile) {
                break
            }
            nextToken = tokenizer.nextToken()
        }

        return currentNode
    }

    fun switchInsertionModeTo(insertionMode: HtmlParserInsertionMode) {
        this.insertionMode = insertionMode
    }

    fun popCurrentNode(): HtmlElement.Node {
        return stackOfOpenElements.pop()
    }

    fun setHeadElementPointer(element: HtmlElement.Node) {

    }

    fun insertHtmlElement(token: HtmlToken.StartTag): HtmlElement.Node  {
        return insertForeignElement(token)
    }

    fun insertForeignElement(token: HtmlToken.StartTag): HtmlElement.Node {
        val adjustedInsertionLocation = findAppropriatePlaceForInsertingNode()
        val element = token.toElement()
        adjustedInsertionLocation.node.children.add(element)
        stackOfOpenElements.push(element)

        return element
    }

    fun insertCharacter(char: Char) {
        val adjustedInsertionLocation = findAppropriatePlaceForInsertingNode()
        val previousSibling = adjustedInsertionLocation.node.children.lastOrNull()

        if (previousSibling is HtmlElement.Text) {
            previousSibling.text += char
        } else {
            adjustedInsertionLocation.node.children.add(HtmlElement.Text(char.toString()))
        }
    }

    fun findAppropriatePlaceForInsertingNode(): AdjustedInsertionLocation {
        return AdjustedInsertionLocation(currentNode)
    }

    fun parseTokenUsingRCDATA(token: HtmlToken.StartTag) {
        insertHtmlElement(token)
        tokenizer.switchStateTo(RCDATAState)
        _originalInsertionMode = insertionMode
        insertionMode = TextInsertionMode
    }

    fun setOriginalInsertionModeToCurrentInsertionMode() {
        _originalInsertionMode = insertionMode
    }

    fun switchToOriginalInsertionMode() {
        insertionMode = originalInsertionMode
    }

    fun reconstructTheActiveFormattingElements() {

    }

    fun setFramesetOkFlagToNotOk() {

    }

    fun insertComment(token: HtmlToken.Comment) {

    }

    fun hasTemplateOnStackOfOpenElements(): Boolean {
        return stackOfOpenElements.any { it.name == "template" }
    }

    fun stopParsing() {
        while (stackOfOpenElements.size > 1) {
            stackOfOpenElements.pop()
        }
    }

    fun generateImpliedEndTags(exceptFor: String) {
        while (currentNode.name != exceptFor && currentNode.name in impliedEndTags) {
            popCurrentNode()
        }
    }

    data class AdjustedInsertionLocation(
        val node: HtmlElement.Node
    )

    companion object {
        private val impliedEndTags = setOf(
            "dd",
            "dt",
            "li",
            "optgroup",
            "option",
            "p",
            "rb",
            "rp",
            "rt",
            "rtc"
        )
    }
}
