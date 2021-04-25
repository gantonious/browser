package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

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
