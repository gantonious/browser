package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState

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
