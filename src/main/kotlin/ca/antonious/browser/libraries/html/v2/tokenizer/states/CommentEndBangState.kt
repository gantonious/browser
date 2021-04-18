package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState

object CommentEndBangState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '-' -> {
                tokenizer.getCurrentToken<HtmlToken.Comment>().comment += "-!"
                tokenizer.switchStateTo(CommentEndDashState)
            }
            nextChar == '>' -> {
                tokenizer.emitError(HtmlParserError.IncorrectlyClosedComment())
                tokenizer.switchStateTo(DataState)
                tokenizer.emitCurrentToken()
            }
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofInComment())
                tokenizer.emitCurrentToken()
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else ->{
                tokenizer.getCurrentToken<HtmlToken.Comment>().comment += "--!"
                tokenizer.reconsumeIn(CommentState)
            }
        }
    }
}