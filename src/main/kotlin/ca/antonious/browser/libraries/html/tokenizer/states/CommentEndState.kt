package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object CommentEndState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '>' -> {
                tokenizer.switchStateTo(DataState)
                tokenizer.emitCurrentToken()
            }
            nextChar == '!' -> {
                tokenizer.switchStateTo(CommentEndBangState)
            }
            nextChar == '-' -> {
                tokenizer.getCurrentToken<HtmlToken.Comment>().comment += '-'
            }
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofInComment())
                tokenizer.emitCurrentToken()
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.getCurrentToken<HtmlToken.Comment>().comment += "--"
                tokenizer.reconsumeIn(CommentState)
            }
        }
    }
}
