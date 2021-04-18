package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState

object RCDATAState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '&' -> {
                tokenizer.setReturnStateTo(RCDATAState)
                tokenizer.switchStateTo(CharacterReferenceState)
            }
            nextChar == '<' -> tokenizer.switchStateTo(RCDATALessThanState)
            nextChar == null -> tokenizer.emitToken(HtmlToken.EndOfFile)
            else -> tokenizer.emitToken(HtmlToken.Character(nextChar))
        }
    }
}
