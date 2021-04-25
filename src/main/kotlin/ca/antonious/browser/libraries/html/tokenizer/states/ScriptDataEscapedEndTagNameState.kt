package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.tokenizer.isHtmlWhiteSpace

object ScriptDataEscapedEndTagNameState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        fun onAnythingElse() {
            tokenizer.emitToken(HtmlToken.Character('<'))
            tokenizer.emitToken(HtmlToken.Character('/'))
            tokenizer.temporaryBuffer.forEach { char ->
                tokenizer.emitToken(HtmlToken.Character(char))
            }
            tokenizer.reconsumeIn(ScriptDataEscapedState)
        }

        when {
            nextChar?.isHtmlWhiteSpace() == true -> {
                if (tokenizer.isCurrentEndTagAnAppropriateEndTagToken()) {
                    tokenizer.switchStateTo(BeforeAttributeNameState)
                } else {
                    onAnythingElse()
                }
            }
            nextChar == '/' -> {
                if (tokenizer.isCurrentEndTagAnAppropriateEndTagToken()) {
                    tokenizer.switchStateTo(SelfClosingStartTagState)
                } else {
                    onAnythingElse()
                }
            }
            nextChar == '>' -> {
                if (tokenizer.isCurrentEndTagAnAppropriateEndTagToken()) {
                    tokenizer.switchStateTo(DataState)
                    tokenizer.emitCurrentToken()
                } else {
                    onAnythingElse()
                }
            }
            nextChar?.isUpperCase() == true -> {
                val lowerCasedChar = nextChar.toLowerCase()
                tokenizer.getCurrentToken<HtmlToken.EndTag>().name += lowerCasedChar
                tokenizer.appendToTemporaryBuffer(lowerCasedChar)
            }
            nextChar?.isLetter() == true -> {
                tokenizer.getCurrentToken<HtmlToken.EndTag>().name += nextChar
                tokenizer.appendToTemporaryBuffer(nextChar)
            }
            else -> onAnythingElse()
        }
    }
}
