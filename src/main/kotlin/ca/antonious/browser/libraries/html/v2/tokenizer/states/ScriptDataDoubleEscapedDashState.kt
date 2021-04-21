package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState

object ScriptDataDoubleEscapedDashState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '-' -> {
                tokenizer.switchStateTo(ScriptDataDoubleEscapedDashDashState)
                tokenizer.emitToken(HtmlToken.Character(nextChar))
            }
            nextChar == '<' -> {
                tokenizer.switchStateTo(ScriptDataDoubleEscapedLessThanSignState)
                tokenizer.emitToken(HtmlToken.Character(nextChar))
            }
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofInScriptHtmlCommentLikeText())
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.switchStateTo(ScriptDataDoubleEscapedState)
                tokenizer.emitToken(HtmlToken.Character(nextChar))
            }
        }
    }
}
