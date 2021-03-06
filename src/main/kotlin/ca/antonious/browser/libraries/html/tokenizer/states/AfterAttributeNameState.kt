package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.tokenizer.isHtmlWhiteSpace

object AfterAttributeNameState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar?.isHtmlWhiteSpace() == true -> Unit
            nextChar == '/' -> tokenizer.switchStateTo(SelfClosingStartTagState)
            nextChar == '=' -> tokenizer.switchStateTo(BeforeAttributeValueState)
            nextChar == '>' -> {
                tokenizer.emitCurrentToken()
                tokenizer.switchStateTo(DataState)
            }
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofInTag())
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.getCurrentToken<HtmlToken.StartTag>().addEmptyAttribute()
                tokenizer.reconsumeIn(AttributeNameState)
            }
        }
    }
}
