package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.tokenizer.isHtmlWhiteSpace

object UnquotedAttributeValueState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar?.isHtmlWhiteSpace() == true -> tokenizer.switchStateTo(BeforeAttributeNameState)
            nextChar == '&' -> {
                tokenizer.setReturnStateTo(UnquotedAttributeValueState)
                tokenizer.switchStateTo(CharacterReferenceState)
            }
            nextChar == '>' -> {
                tokenizer.switchStateTo(DataState)
                tokenizer.emitCurrentToken()
            }
            nextChar == '"' ||
            nextChar == '\'' ||
            nextChar == '<' ||
            nextChar == '=' ||
            nextChar == '`' -> {
                tokenizer.emitError(HtmlParserError.UnexpectedCharacterInUnquotedAttributeValue())
                tokenizer.getCurrentToken<HtmlToken.StartTag>().currentAttribute.value += nextChar
            }
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofInTag())
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.getCurrentToken<HtmlToken.StartTag>().currentAttribute.value += nextChar
            }
        }
    }
}
