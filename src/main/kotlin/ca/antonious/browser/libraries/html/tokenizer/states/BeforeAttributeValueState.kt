package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.tokenizer.isHtmlWhiteSpace

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
