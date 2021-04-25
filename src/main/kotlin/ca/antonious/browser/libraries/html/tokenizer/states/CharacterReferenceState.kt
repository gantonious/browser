package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object CharacterReferenceState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        tokenizer.resetTemporaryBuffer()
        tokenizer.appendToTemporaryBuffer('&')
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar?.isLetterOrDigit() == true -> tokenizer.reconsumeIn(NamedCharacterReferenceState)
            nextChar == '#' -> {
                tokenizer.appendToTemporaryBuffer(nextChar)
                tokenizer.switchStateTo(NumericCharacterReferenceState)
            }
            else -> {
                tokenizer.flushCodePointsConsumedAsACharacterReference()
                tokenizer.reconsumeInReturnState()
            }
        }
    }
}
