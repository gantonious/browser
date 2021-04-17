package ca.antonious.browser.libraries.html.v2.states

import ca.antonious.browser.libraries.html.v2.HtmlToken
import ca.antonious.browser.libraries.html.v2.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.HtmlTokenizerState

object TagNameState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        when (val nextChar = tokenizer.consumeNextChar()) {
            '\t', '\n', '\u000C', ' ' -> {
                tokenizer.switchStateTo(BeforeAttributeNameState)
            }
            '/' -> {
                tokenizer.switchStateTo(SelfClosingStartTagState)
            }
            '>' -> {
                tokenizer.emitCurrentToken()
            }
            null -> {
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.getCurrentToken<HtmlToken.StartTag>().name += nextChar
            }
        }
    }
}
