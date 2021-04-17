package ca.antonious.browser.libraries.html.v2.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.HtmlToken
import ca.antonious.browser.libraries.html.v2.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.HtmlTokenizerState

object BeforeAttributeNameState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        when (tokenizer.consumeNextChar()) {
            '\t', '\n', '\u000C', ' ' -> Unit
            '/', '>', null -> tokenizer.reconsumeIn(AfterAttributeNameState)
            '=' -> {
                tokenizer.emitError(HtmlParserError.UnexpectedEqualsSignBeforeAttributeName())
            }
            else -> {
                tokenizer.getCurrentToken<HtmlToken.StartTag>()
                tokenizer.reconsumeIn(AttributeNameState)
            }
        }
    }
}
