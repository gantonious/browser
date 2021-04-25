package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object CommentLessThanSignState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '!' -> {
                tokenizer.getCurrentToken<HtmlToken.Comment>().comment += nextChar
                tokenizer.switchStateTo(CommentLessThanSignBangState)
            }
            nextChar == '<' -> {
                tokenizer.getCurrentToken<HtmlToken.Comment>().comment += nextChar
            }
            else -> tokenizer.reconsumeIn(CommentState)
        }
    }
}
