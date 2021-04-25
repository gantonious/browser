package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object ScriptDataDoubleEscapedLessThanSignState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '/' -> {
                tokenizer.resetTemporaryBuffer()
                tokenizer.switchStateTo(ScriptDataDoubleEscapeEndState)
            }
            else -> {
                tokenizer.reconsumeIn(ScriptDataDoubleEscapedState)
            }
        }
    }
}
