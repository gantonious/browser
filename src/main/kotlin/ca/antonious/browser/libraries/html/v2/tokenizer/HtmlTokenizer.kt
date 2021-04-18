package ca.antonious.browser.libraries.html.v2.tokenizer

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.states.DataState
import kotlin.math.min

class HtmlTokenizer(val source: String) {
    private var cursor = 0
    private var returnState: HtmlTokenizerState? = null
    private var state: HtmlTokenizerState = DataState
    var currentToken: HtmlToken? = null
    private var emittedTokenQueue = mutableListOf<HtmlToken>()
    var temporaryBuffer = ""

    var lastEmittedStartTag: HtmlToken.StartTag? = null

    fun nextToken(): HtmlToken {
        if (emittedTokenQueue.isNotEmpty()) {
            return emittedTokenQueue.removeAt(0)
        }

        while (emittedTokenQueue.isEmpty()) {
            state.tickState(this)
            returnState?.let { switchStateTo(it) }
        }

        return emittedTokenQueue.removeAt(0)
    }

    fun consumeNextChar(): Char? {
        if (cursor >= source.length) {
            return null
        }

        return source[cursor++]
    }

    fun peekNextNChars(n: Int): String {
        if (isAtEof()) {
            return ""
        }

        return source.substring(cursor, min(cursor + n, source.length))
    }

    fun consumeNextNChars(n: Int) {
        cursor += n
    }

    fun isAtEof(): Boolean {
        return cursor == source.length
    }

    fun setReturnStateTo(state: HtmlTokenizerState) {
        returnState = state
    }

    fun switchStateTo(state: HtmlTokenizerState) {
        this.state = state
    }

    fun reconsumeIn(state: HtmlTokenizerState) {
        cursor--
        this.state = state
    }

    fun emitError(error: HtmlParserError) {

    }

    fun createToken(token: HtmlToken) {
        this.currentToken = token

    }
    fun emitToken(token: HtmlToken) {
        emittedTokenQueue.add(token)

        if (token is HtmlToken.StartTag) {
            lastEmittedStartTag = token
        }
    }

    fun resetTemporaryBuffer() {
        temporaryBuffer = ""
    }

    fun appendToTemporaryBuffer(char: Char) {
        temporaryBuffer += char
    }

    fun flushCodePointsConsumedAsACharacterReference() {
        for (char in temporaryBuffer) {
            emitToken(HtmlToken.Character(char))
        }
    }

    fun isCurrentEndTagAnAppropriateEndTagToken(): Boolean {
        return  lastEmittedStartTag?.name == getCurrentToken<HtmlToken.EndTag>().name
    }

    inline fun <reified T> getCurrentToken(): T {
        return currentToken as T
    }

    fun emitCurrentToken() {
        emitToken(currentToken!!)
        currentToken = null
    }
}
