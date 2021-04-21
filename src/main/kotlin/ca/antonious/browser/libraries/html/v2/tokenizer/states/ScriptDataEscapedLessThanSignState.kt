package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState

object ScriptDataEscapedLessThanSignState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '/' -> {
                tokenizer.resetTemporaryBuffer()
                tokenizer.switchStateTo(ScriptDataEscapedEndTagOpenState)
            }
            nextChar?.isLetter() == true -> {
                tokenizer.resetTemporaryBuffer()
                tokenizer.emitToken(HtmlToken.Character('<'))
                tokenizer.reconsumeIn(ScriptDataDoubleEscapeStartState)
            }
            else -> {
                tokenizer.emitToken(HtmlToken.Character('<'))
                tokenizer.reconsumeIn(ScriptDataEscapedState)
            }
        }
    }
}
