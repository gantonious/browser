package ca.antonious.browser.libraries.html.v2.states

import ca.antonious.browser.libraries.html.v2.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.HtmlTokenizerState

object CharacterReferenceState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        tokenizer.resetTemporaryBuffer()

        val nextChar = tokenizer.consumeNextChar()
        when {
            nextChar?.isLetterOrDigit() == true -> tokenizer.reconsumeIn(NamedCharacterReferenceState)
            nextChar == '#' -> {
                tokenizer.appendToTemporaryBuffer(nextChar)
                tokenizer.switchStateTo(NumericCharacterReferenceState)
            }

        }
    }
}
