package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object CommentLessThanSignBangDashDashState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '>' -> tokenizer.reconsumeIn(CommentEndState)
            else ->{
                tokenizer.emitError(HtmlParserError.NestedComment())
                tokenizer.reconsumeIn(CommentEndState)
            }
        }
    }
}
