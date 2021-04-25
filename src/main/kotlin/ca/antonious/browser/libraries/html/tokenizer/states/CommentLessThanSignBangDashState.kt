package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object CommentLessThanSignBangDashState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '-' -> tokenizer.switchStateTo(CommentLessThanSignBangDashDashState)
            else -> tokenizer.reconsumeIn(CommentEndDashState)
        }
    }
}
