package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.tokenizer.isHtmlWhiteSpace

object BeforeAttributeNameState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()
        when  {
            nextChar?.isHtmlWhiteSpace() == true -> Unit
            nextChar == '/' ||
            nextChar == '>' ||
            nextChar == null -> {
                tokenizer.reconsumeIn(AfterAttributeNameState)
            }
            nextChar == '=' -> {
                tokenizer.emitError(HtmlParserError.UnexpectedEqualsSignBeforeAttributeName())
            }
            else -> {
                tokenizer.getCurrentToken<HtmlToken.StartTag>().addEmptyAttribute()
                tokenizer.reconsumeIn(AttributeNameState)
            }
        }
    }
}
