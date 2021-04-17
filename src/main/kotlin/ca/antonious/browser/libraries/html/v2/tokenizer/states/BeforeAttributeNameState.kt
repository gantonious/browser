package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.v2.tokenizer.isHtmlWhiteSpace

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
