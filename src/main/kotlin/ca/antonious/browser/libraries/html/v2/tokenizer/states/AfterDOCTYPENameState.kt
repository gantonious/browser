package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.v2.tokenizer.isHtmlWhiteSpace

object AfterDOCTYPENameState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar?.isHtmlWhiteSpace() == true -> Unit
            nextChar == '>' -> {
                tokenizer.emitCurrentToken()
                tokenizer.switchStateTo(DataState)
            }
            nextChar == null -> {
                tokenizer.getCurrentToken<HtmlToken.Doctype>().forceQuirks = true
                tokenizer.emitCurrentToken()
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                val current6Chars = tokenizer.peekCurrentNChars(6).toUpperCase()

                when (current6Chars) {
                    "PUBLIC" -> {
                        tokenizer.consumeNextNChars(5)
                        tokenizer.switchStateTo(AfterDOCTYPEPublicKeywordState)
                    }
                    "SYSTEM" -> {
                        tokenizer.consumeNextNChars(5)
                        tokenizer.switchStateTo(AfterDOCTYPESystemKeywordState)
                    }
                    else -> {
                        tokenizer.emitError(HtmlParserError.InvalidCharacterSequenceAfterDoctypeName())
                        tokenizer.getCurrentToken<HtmlToken.Doctype>().forceQuirks = true
                        tokenizer.reconsumeIn(BogusDOCTYPEState)
                    }
                }
            }
        }
    }
}
