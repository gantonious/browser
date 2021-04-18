package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState

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
