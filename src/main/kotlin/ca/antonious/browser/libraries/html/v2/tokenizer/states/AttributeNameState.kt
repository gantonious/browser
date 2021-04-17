package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState
import ca.antonious.browser.libraries.html.v2.tokenizer.isHtmlWhiteSpace

object AttributeNameState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()

        when {
            nextChar?.isHtmlWhiteSpace() == true ||
            nextChar == '/' ||
            nextChar == '>' -> {
                tokenizer.reconsumeIn(AfterAttributeNameState)
            }
            nextChar == '=' -> tokenizer.reconsumeIn(AfterAttributeNameState)
            nextChar?.isUpperCase() == true -> {
                tokenizer.getCurrentToken<HtmlToken.StartTag>().currentAttribute.name += nextChar.toLowerCase()
            }
            nextChar == '"' ||
            nextChar == '\'' ||
            nextChar == '<' -> {
                tokenizer.emitError(HtmlParserError.UnexpectedCharacterInAttributeNameError())
                tokenizer.getCurrentToken<HtmlToken.StartTag>().currentAttribute.name += nextChar
            }
            else -> {
                tokenizer.getCurrentToken<HtmlToken.StartTag>().currentAttribute.name  += nextChar
            }
        }
    }
}
