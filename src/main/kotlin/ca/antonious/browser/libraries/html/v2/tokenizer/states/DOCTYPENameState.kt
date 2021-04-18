package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.v2.tokenizer.isHtmlWhiteSpace

object DOCTYPENameState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar?.isHtmlWhiteSpace() == true -> tokenizer.switchStateTo(AfterDOCTYPENameState)
            nextChar == '>' -> {
                tokenizer.emitCurrentToken()
                tokenizer.switchStateTo(DataState)
            }
            nextChar?.isUpperCase() == true -> {
                tokenizer.getCurrentToken<HtmlToken.Doctype>().name += nextChar.toLowerCase()
            }
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofInDoctype())
                tokenizer.emitToken(HtmlToken.Doctype(forceQuirks = true))
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.getCurrentToken<HtmlToken.Doctype>().name += nextChar
            }
        }
    }
}
