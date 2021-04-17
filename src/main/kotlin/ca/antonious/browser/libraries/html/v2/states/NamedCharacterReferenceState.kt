package ca.antonious.browser.libraries.html.v2.states

import ca.antonious.browser.libraries.html.v2.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.HtmlTokenizerState

object NamedCharacterReferenceState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        // todo look up named characters
        tokenizer.flushCodePointsConsumedAsACharacterReference()
    }
}
