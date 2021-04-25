package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object AmbiguousAmpersandState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar?.isLetterOrDigit() == true -> {
                if (tokenizer.isCharacterReferenceConsumedAsPartOfAnAttribute()) {
                    tokenizer.getCurrentToken<HtmlToken.StartTag>().currentAttribute.value += nextChar
                } else {
                    tokenizer.emitToken(HtmlToken.Character(nextChar))
                }
            }
            nextChar == ';' -> {
                tokenizer.emitError(HtmlParserError.UnknownNamedCharacterReference())
                tokenizer.reconsumeInReturnState()
            }
            else -> {
                tokenizer.reconsumeInReturnState()
            }
        }
    }
}
