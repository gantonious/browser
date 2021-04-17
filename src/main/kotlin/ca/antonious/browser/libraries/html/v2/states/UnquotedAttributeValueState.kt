package ca.antonious.browser.libraries.html.v2.states

import ca.antonious.browser.libraries.html.v2.*

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
