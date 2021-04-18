package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState

object CommentState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '<' -> {
                tokenizer.getCurrentToken<HtmlToken.Comment>().comment += nextChar
                tokenizer.switchStateTo(CommentLessThanSignState)
            }
            nextChar == '-' -> {
                tokenizer.switchStateTo(CommentEndDashState)
            }
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofInComment())
                tokenizer.emitCurrentToken()
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.getCurrentToken<HtmlToken.Comment>().comment += nextChar
            }
        }
    }
}
