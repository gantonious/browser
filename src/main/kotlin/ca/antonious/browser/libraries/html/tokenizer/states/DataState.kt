package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object DataState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        if (tokenizer.isAtEof()) {
            tokenizer.emitToken(HtmlToken.EndOfFile)
        }

        when (val nextChar = tokenizer.consumeNextChar()) {
            '&' -> {
                tokenizer.setReturnStateTo(DataState)
                tokenizer.switchStateTo(CharacterReferenceState)
            }
            '<' -> {
                tokenizer.switchStateTo(TagOpenState)
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
