package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object HexadecimalCharacterReferenceStartState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar?.isDigit() == true -> tokenizer.reconsumeIn(HexadecimalCharacterReferenceState)
            else -> {
                tokenizer.emitError(HtmlParserError.AbsenceOfDigitsInNumericCharacterReference())
                tokenizer.flushCodePointsConsumedAsACharacterReference()
                tokenizer.reconsumeInReturnState()
            }
        }
    }
}
