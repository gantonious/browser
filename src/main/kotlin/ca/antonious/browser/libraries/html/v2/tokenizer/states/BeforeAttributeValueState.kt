package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.v2.tokenizer.isHtmlWhiteSpace

object BeforeAttributeValueState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()
        when {
            nextChar?.isHtmlWhiteSpace() == true -> Unit
            nextChar == '"' -> tokenizer.switchStateTo(DoubleQuotedAttributeValueState)
            nextChar == '\'' -> tokenizer.switchStateTo(SingleQuotedAttributeValueState)
            nextChar == '>' -> {
                tokenizer.emitError(HtmlParserError.MissingAttributeValue())
                tokenizer.switchStateTo(DataState)
                tokenizer.emitCurrentToken()
            }
            else -> tokenizer.reconsumeIn(UnquotedAttributeValueState)
        }
    }
}
