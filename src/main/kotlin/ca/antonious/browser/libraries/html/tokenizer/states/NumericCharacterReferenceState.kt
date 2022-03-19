package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object NumericCharacterReferenceState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        tokenizer.characterReferenceCode = 0

        when (val nextInputChar = tokenizer.consumeNextChar()) {
            'x', 'X' -> {
                tokenizer.appendToTemporaryBuffer(nextInputChar)
                tokenizer.switchStateTo(HexadecimalCharacterReferenceStartState)
            }
            else -> tokenizer.reconsumeIn(DecimalCharacterReferenceStartState)
        }
    }
}
