package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState

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
