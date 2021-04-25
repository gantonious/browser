package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object SelfClosingStartTagState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val nextChar = tokenizer.consumeNextChar()
        when {
            nextChar == '>' -> {
                tokenizer.getCurrentToken<HtmlToken.StartTag>().selfClosing = true
                tokenizer.switchStateTo(DataState)
                tokenizer.emitCurrentToken()
            }
            nextChar == null -> {
                tokenizer.emitError(HtmlParserError.EofInTag())
                tokenizer.emitToken(HtmlToken.EndOfFile)
            }
            else -> {
                tokenizer.emitError(HtmlParserError.UnexpectedSolidusInTag())
                tokenizer.reconsumeIn(BeforeAttributeNameState)
            }
        }
    }
}
