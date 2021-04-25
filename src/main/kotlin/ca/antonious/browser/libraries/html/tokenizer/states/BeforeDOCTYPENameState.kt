package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.tokenizer.isHtmlWhiteSpace

object BeforeDOCTYPENameState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar?.isHtmlWhiteSpace() == true -> Unit
            nextChar?.isUpperCase() == true -> {
                tokenizer.createToken(HtmlToken.Doctype(name = nextChar.toLowerCase().toString()))
                tokenizer.switchStateTo(DOCTYPENameState)
            }
            nextChar == '>' -> {
                tokenizer.emitError(HtmlParserError.MissingDoctypeName())
                tokenizer.emitToken(HtmlToken.Doctype(forceQuirks = true))
                tokenizer.switchStateTo(DataState)
            }
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofInDoctype())
                tokenizer.emitToken(HtmlToken.Doctype(forceQuirks = true))
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.createToken(HtmlToken.Doctype(name = nextChar.toString()))
                tokenizer.switchStateTo(DOCTYPENameState)
            }
        }
    }
}
