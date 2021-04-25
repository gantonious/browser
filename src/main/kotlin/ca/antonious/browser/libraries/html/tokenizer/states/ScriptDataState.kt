package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object ScriptDataState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '<' -> tokenizer.switchStateTo(ScriptDataLessThanSignState)
            nextChar == null -> tokenizer.emitToken(HtmlToken.EndOfFile)
            else -> tokenizer.emitToken(HtmlToken.Character(nextChar))
        }
    }
}
