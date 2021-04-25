package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object CommentStartState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '-' -> tokenizer.switchStateTo(CommentStartDashState)
            nextChar == '>' -> {
                tokenizer.emitError(HtmlParserError.AbruptClosingOfEmptyComment())
                tokenizer.switchStateTo(DataState)
                tokenizer.emitCurrentToken()
            }
            else -> {
                tokenizer.reconsumeIn(CommentState)
            }
        }
    }
}
