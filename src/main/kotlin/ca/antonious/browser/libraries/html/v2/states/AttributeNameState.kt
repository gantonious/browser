package ca.antonious.browser.libraries.html.v2.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.HtmlToken
import ca.antonious.browser.libraries.html.v2.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.HtmlTokenizerState

object AttributeNameState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()
        when {
            nextChar == '\t' ||
            nextChar == '\n' ||
            nextChar == '\u000C' ||
            nextChar == ' ' ||
            nextChar == '/' ||
            nextChar == '>' -> {
                tokenizer.reconsumeIn(AfterAttributeNameState)
            }
            nextChar == '=' -> tokenizer.reconsumeIn(AfterAttributeNameState)
            nextChar?.isUpperCase() == true -> {
                tokenizer.getCurrentToken<HtmlToken.StartTag>().currentAttributeName += nextChar.toLowerCase()
            }
            nextChar == '"' ||
            nextChar == '\'' ||
            nextChar == '<' -> {
                tokenizer.emitError(HtmlParserError.UnexpectedCharacterInAttributeNameError())
                tokenizer.getCurrentToken<HtmlToken.StartTag>().currentAttributeName += nextChar
            }
            else -> {
                tokenizer.getCurrentToken<HtmlToken.StartTag>().currentAttributeName += nextChar
            }
        }
    }
}
