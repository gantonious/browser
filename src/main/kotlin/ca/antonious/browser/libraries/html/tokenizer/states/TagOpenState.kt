package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object TagOpenState : HtmlTokenizerState {
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
                tokenizer.emitToken(HtmlToken.Character('<'))
                tokenizer.reconsumeIn(DataState)
            }
        }
    }
}
