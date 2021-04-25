package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object DoubleQuotedAttributeValueState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()
        when {
            nextChar == '"' -> tokenizer.switchStateTo(AfterQuotedAttributeValueState)
            nextChar == '&' -> {
                tokenizer.setReturnStateTo(DoubleQuotedAttributeValueState)
                tokenizer.switchStateTo(CharacterReferenceState)
            }
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofInTag())
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.getCurrentToken<HtmlToken.StartTag>().currentAttribute.value += nextChar
            }
        }
    }
}
