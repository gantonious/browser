package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object ScriptDataEscapedEndTagOpenState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar?.isLetter() == true -> {
                tokenizer.createToken(HtmlToken.EndTag())
                tokenizer.reconsumeIn(ScriptDataEscapedEndTagNameState)
            }
            else -> {
                tokenizer.emitToken(HtmlToken.Character('<'))
                tokenizer.emitToken(HtmlToken.Character('/'))
                tokenizer.reconsumeIn(ScriptDataEscapedState)
            }
        }
    }
}
