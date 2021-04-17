package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState

object EndTagOpenState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar?.isLetter() == true -> {
                tokenizer.createToken(HtmlToken.EndTag())
                tokenizer.reconsumeIn(TagNameState)
            }
            nextChar == '>' -> {
                tokenizer.emitError(HtmlParserError.EofBeforeTagName())
                tokenizer.emitToken(HtmlToken.Character('<'))
                tokenizer.emitToken(HtmlToken.Character('/'))
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
        }
    }
}
