package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object BogusDOCTYPEState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '>' -> {
                tokenizer.switchStateTo(DataState)
                tokenizer.emitCurrentToken()
            }
            nextChar == null -> {
                tokenizer.emitCurrentToken()
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> Unit
        }
    }
}
