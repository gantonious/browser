package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState

object ScriptDataDoubleEscapedState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '-' -> {
                tokenizer.switchStateTo(ScriptDataDoubleEscapedDashState)
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
                tokenizer.emitToken(HtmlToken.Character(nextChar))
            }
        }
    }
}
