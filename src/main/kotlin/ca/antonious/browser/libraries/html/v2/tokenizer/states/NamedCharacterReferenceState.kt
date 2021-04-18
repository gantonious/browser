package ca.antonious.browser.libraries.html.v2.tokenizer.states

import ca.antonious.browser.libraries.html.v2.HtmlParserError
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizerState

object NamedCharacterReferenceState : HtmlTokenizerState {
    private val namedCharacterReferences = mapOf(
        "nbsp;" to charArrayOf('\u00A0'),
        "nbsp" to charArrayOf('\u00A0')
    )

    override fun tickState(tokenizer: HtmlTokenizer) {
        val textToMatch = tokenizer.peekNextNChars(20)

        val match = namedCharacterReferences.entries.firstOrNull {
            textToMatch.startsWith(it.key)
        }

        if (match != null) {
            match.key.forEach { tokenizer.appendToTemporaryBuffer(it) }
            tokenizer.consumeNextNChars(match.key.length)

            val nextChar = tokenizer.peekNextNChars(1).toCharArray().first()
            val isLastCharOfMatchASemiColon = match.key.last() == ';'

            if (
                tokenizer.isCharacterReferenceConsumedAsPartOfAnAttribute() &&
                !isLastCharOfMatchASemiColon &&
                nextChar == '=' || nextChar.isLetterOrDigit()
            ) {
                tokenizer.flushCodePointsConsumedAsACharacterReference()
                tokenizer.switchToReturnState()
            } else {
                if (!isLastCharOfMatchASemiColon) {
                    tokenizer.emitError(HtmlParserError.MissingSemiColonAfterCharacterReference())
                }

                tokenizer.resetTemporaryBuffer()
                match.value.forEach { tokenizer.appendToTemporaryBuffer(it) }
                tokenizer.flushCodePointsConsumedAsACharacterReference()
                tokenizer.switchToReturnState()
            }
        } else {
            tokenizer.flushCodePointsConsumedAsACharacterReference()
            tokenizer.switchStateTo(AmbiguousAmpersandState)
        }
    }
}
