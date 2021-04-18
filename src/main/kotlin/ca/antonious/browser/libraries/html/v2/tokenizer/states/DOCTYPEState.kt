package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.v2.tokenizer.isHtmlWhiteSpace

object DOCTYPEState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()
        when {
            nextChar?.isHtmlWhiteSpace() == true -> tokenizer.switchStateTo(BeforeDOCTYPENameState)
            nextChar == '>' -> tokenizer.reconsumeIn(BeforeDOCTYPENameState)
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofInDoctype())
                tokenizer.emitToken(HtmlToken.Doctype(forceQuirks = true))
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.emitError(HtmlParserError.MissingWhitespaceBeforeDoctypeName())
                tokenizer.reconsumeIn(BeforeDOCTYPENameState)
            }
        }
    }
}
