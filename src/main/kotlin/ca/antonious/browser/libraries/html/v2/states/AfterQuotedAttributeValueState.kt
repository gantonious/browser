package ca.antonious.browser.libraries.html.v2.states

import ca.antonious.browser.libraries.html.v2.*

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
