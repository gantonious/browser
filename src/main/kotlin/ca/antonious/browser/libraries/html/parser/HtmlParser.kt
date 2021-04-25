package ca.antonious.browser.libraries.html.parser

import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.html.parser.insertionmodes.*
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.states.RAWTEXTState
import ca.antonious.browser.libraries.html.tokenizer.states.RCDATAState
import ca.antonious.browser.libraries.html.tokenizer.states.ScriptDataState
import java.util.*

class HtmlParser(source: String) {
    val tokenizer = HtmlTokenizer(source)
    private var headElement: HtmlElement.Node? = null
    private var _originalInsertionMode: HtmlParserInsertionMode? = null
    private var insertionMode: HtmlParserInsertionMode = InitialInsertionMode
    var stackOfOpenElements = Stack<HtmlElement.Node>()

    private var fragmentCaseContext: HtmlElement.Node? = null

    private val isFragmentCase: Boolean
        get() = fragmentCaseContext != null

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

    fun parseAsFragment(context: HtmlElement.Node): List<HtmlElement> {
        fragmentCaseContext = context

        when (context.name) {
            "title", "textarea" -> {
                tokenizer.switchStateTo(RCDATAState)
            }
            "style", "xmp", "iframe", "noembed", "noframes" -> {
                tokenizer.switchStateTo(RAWTEXTState)
            }
            "script" -> {
                tokenizer.switchStateTo(ScriptDataState)
            }
        }

        val rootElement = HtmlElement.Node(name = "html")
        stackOfOpenElements.push(rootElement)

        resetInsertionModeAppropriately()
        parse()

        return rootElement.children
    }

    fun switchInsertionModeTo(insertionMode: HtmlParserInsertionMode) {
        this.insertionMode = insertionMode
    }

    fun popCurrentNode(): HtmlElement.Node {
        return stackOfOpenElements.pop()
    }

    fun setHeadElementPointer(element: HtmlElement.Node) {
        headElement = element
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

    fun parseTokenUsingRAWTEXT(token: HtmlToken.StartTag) {
        insertHtmlElement(token)
        tokenizer.switchStateTo(RAWTEXTState)
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

    fun resetInsertionModeAppropriately() {
        for (element in stackOfOpenElements.reversed()) {
            val last = element == stackOfOpenElements.lastOrNull()

            val node = if (last && isFragmentCase) {
                fragmentCaseContext!!
            } else {
                element
            }

            when {
                node.name == "head" && !last -> {
                    switchInsertionModeTo(InHeadInsertionMode)
                    return
                }
                node.name == "html" -> {
                    if (headElement == null) {
                        switchInsertionModeTo(BeforeHeadInsertionMode)
                        return
                    } else {
                        switchInsertionModeTo(AfterHeadInsertionMode)
                        return
                    }
                }
                last -> {
                    switchInsertionModeTo(InBodyInsertionMode)
                    return
                }
            }
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
