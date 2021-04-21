package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.v2.tokenizer.isHtmlWhiteSpace

object ScriptDataDoubleEscapeEndState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar?.isHtmlWhiteSpace() == true ||
                    nextChar == '/' ||
                    nextChar == '>' -> {
                if (tokenizer.temporaryBuffer == "script") {
                    tokenizer.switchStateTo(ScriptDataEscapedState)
                } else {
                    tokenizer.switchStateTo(ScriptDataDoubleEscapedState)
                }

                tokenizer.emitToken(HtmlToken.Character(nextChar))
            }
            nextChar?.isUpperCase() == true -> {
                tokenizer.appendToTemporaryBuffer(nextChar.toLowerCase())
                tokenizer.emitToken(HtmlToken.Character(nextChar))
            }
            nextChar?.isLetter() == true -> {
                tokenizer.emitToken(HtmlToken.Character(nextChar))
            }
            else -> {
                tokenizer.reconsumeIn(ScriptDataDoubleEscapedState)
            }
        }
    }
}
