package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object ScriptDataEscapedState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '-' -> {
                tokenizer.switchStateTo(ScriptDataEscapedDashState)
                tokenizer.emitToken(HtmlToken.Character(nextChar))
            }
            nextChar == '<' -> {
                tokenizer.switchStateTo(ScriptDataEscapedLessThanSignState)
            }
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofInScriptHtmlCommentLikeText())
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.emitToken(HtmlToken.Character(nextChar))
            }
        }
    }
}
