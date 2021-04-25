package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.tokenizer.isHtmlWhiteSpace

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
