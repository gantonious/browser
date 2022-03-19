package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.isLowerHexDigit
import ca.antonious.browser.libraries.html.isUpperHexDigit
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object HexadecimalCharacterReferenceState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextInputChar = tokenizer.consumeNextChar()

        when {
            nextInputChar?.isDigit() == true -> {
                tokenizer.characterReferenceCode *= 16
                tokenizer.characterReferenceCode += nextInputChar.code - 0x0030
            }
            nextInputChar?.isUpperHexDigit() == true -> {
                tokenizer.characterReferenceCode *= 16
                tokenizer.characterReferenceCode += nextInputChar.code - 0x0037
            }
            nextInputChar?.isLowerHexDigit() == true -> {
                tokenizer.characterReferenceCode *= 16
                tokenizer.characterReferenceCode += nextInputChar.code - 0x0057
            }
            nextInputChar == ';' -> tokenizer.switchStateTo(NumericCharacterReferenceEndState)
            else -> {
                tokenizer.emitError(HtmlParserError.MissingSemiColonAfterCharacterReference())
                tokenizer.reconsumeIn(NumericCharacterReferenceEndState)
            }
        }
    }
}
