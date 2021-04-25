package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object MarkupDeclarationOpenState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val next7Chars = tokenizer.peekNextNChars(7)
        when {
            next7Chars.startsWith("--") -> {
                tokenizer.consumeNextNChars(2)
                tokenizer.createToken(HtmlToken.Comment())
                tokenizer.switchStateTo(CommentStartState)
            }
            next7Chars.toLowerCase() == "doctype" -> {
                tokenizer.consumeNextNChars(7)
                tokenizer.switchStateTo(DOCTYPEState)
            }
            else -> {
                tokenizer.emitError(HtmlParserError.IncorrectlyOpenedComment())
                tokenizer.createToken(HtmlToken.Comment())
                tokenizer.switchStateTo(BogusCommentState)
            }
        }
    }
}
