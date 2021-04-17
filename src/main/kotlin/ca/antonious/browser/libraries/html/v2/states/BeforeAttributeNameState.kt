package ca.antonious.browser.libraries.html.v2.states

import ca.antonious.browser.libraries.html.v2.*

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
