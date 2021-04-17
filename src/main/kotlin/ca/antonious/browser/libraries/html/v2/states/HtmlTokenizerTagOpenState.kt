package ca.antonious.browser.libraries.html.v2.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.HtmlToken
import ca.antonious.browser.libraries.html.v2.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.HtmlTokenizerState

object HtmlTokenizerTagOpenState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar == '!' -> tokenizer.switchStateTo(MarkupDeclarationOpenState)
            nextChar == '/' -> tokenizer.switchStateTo(EndTagOpenState)
            nextChar?.isLetter() == true -> {
                tokenizer.createToken(HtmlToken.StartTag())
                tokenizer.reconsumeIn(TagNameState)
            }
            nextChar == '?' -> {
                tokenizer.emitError(HtmlParserError.UnexpectedQuestionMarkBeforeTagName())
                tokenizer.createToken(HtmlToken.Comment(""))
                tokenizer.reconsumeIn(BogusCommentState)
            }
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofBeforeTagName())
                tokenizer.emitToken(HtmlToken.Character('<'))
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.emitError(HtmlParserError.InvalidFirstCharacterOfTagName())
                tokenizer.reconsumeIn(DataState)
            }
        }
    }
}
