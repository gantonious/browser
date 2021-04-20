package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState

object RAWTEXTLessThanSignState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '/' -> {
                tokenizer.resetTemporaryBuffer()
                tokenizer.switchStateTo(RAWTEXTEndTagOpenState)
            }
            else -> {
                tokenizer.emitToken(HtmlToken.Character('<'))
                tokenizer.reconsumeIn(RAWTEXTState)
            }
        }
    }
}
