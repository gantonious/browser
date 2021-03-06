package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.tokenizer.isHtmlWhiteSpace

object ScriptDataDoubleEscapeStartState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar?.isHtmlWhiteSpace() == true ||
            nextChar == '/' ||
            nextChar == '>' -> {
                if (tokenizer.temporaryBuffer == "script") {
                    tokenizer.switchStateTo(ScriptDataDoubleEscapedState)
                } else {
                    tokenizer.switchStateTo(ScriptDataEscapedState)
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
                tokenizer.reconsumeIn(ScriptDataEscapedState)
            }
        }
    }
}
