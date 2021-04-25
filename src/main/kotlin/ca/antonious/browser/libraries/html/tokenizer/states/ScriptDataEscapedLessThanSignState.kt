package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

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
