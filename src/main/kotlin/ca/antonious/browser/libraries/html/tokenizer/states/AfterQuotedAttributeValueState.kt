package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.tokenizer.isHtmlWhiteSpace

object AfterQuotedAttributeValueState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar?.isHtmlWhiteSpace() == true -> tokenizer.switchStateTo(BeforeAttributeNameState)
            nextChar == '/' -> tokenizer.switchStateTo(SelfClosingStartTagState)
            nextChar == '>' -> {
                tokenizer.switchStateTo(DataState)
                tokenizer.emitCurrentToken()
            }
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofInTag())
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.emitError(HtmlParserError.MissingWhitespaceBetweenAttributes())
                tokenizer.reconsumeIn(BeforeAttributeNameState)
            }
        }
    }
}
