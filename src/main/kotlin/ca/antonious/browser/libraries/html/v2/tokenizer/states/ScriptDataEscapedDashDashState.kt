package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState

object ScriptDataEscapedDashDashState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '-' -> {
                tokenizer.emitToken(HtmlToken.Character(nextChar))
            }
            nextChar == '<' -> {
                tokenizer.switchStateTo(ScriptDataEscapedLessThanSignState)
            }
            nextChar == '>' -> {
                tokenizer.switchStateTo(ScriptDataState)
                tokenizer.emitToken(HtmlToken.Character(nextChar))
            }
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofInScriptHtmlCommentLikeText())
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.switchStateTo(ScriptDataEscapedState)
                tokenizer.emitToken(HtmlToken.Character(nextChar))
            }
        }
    }
}
