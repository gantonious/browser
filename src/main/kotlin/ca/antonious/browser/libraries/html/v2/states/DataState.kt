package ca.antonious.browser.libraries.html.v2.states

import ca.antonious.browser.libraries.html.v2.HtmlToken
import ca.antonious.browser.libraries.html.v2.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.HtmlTokenizerState

object DataState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        if (tokenizer.isAtEof()) {
            tokenizer.emitToken(HtmlToken.EndOfFile)
        }

        when (val nextChar = tokenizer.consumeNextChar()) {
            '&' -> {
                tokenizer.setReturnState(DataState)
                tokenizer.switchStateTo(CharacterReferenceState)
            }
            '<' -> {
                tokenizer.switchStateTo(HtmlTokenizerTagOpenState)
            }
            null -> {
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.emitToken(HtmlToken.Character(nextChar))
            }
        }
    }
}
