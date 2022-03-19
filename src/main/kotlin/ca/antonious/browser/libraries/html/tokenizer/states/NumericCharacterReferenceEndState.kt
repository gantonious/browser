package ca.antonious.browser.libraries.html.tokenizer.states

import ca.antonious.browser.libraries.html.HtmlParserError
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizer
import ca.antonious.browser.libraries.html.tokenizer.HtmlTokenizerState

object NumericCharacterReferenceEndState : HtmlTokenizerState {
    override fun tickState(tokenizer: HtmlTokenizer) {
        val characterReferenceCode = tokenizer.characterReferenceCode

        when {
            characterReferenceCode == 0 -> {
                tokenizer.emitError(HtmlParserError.NullCharacterReference())
                tokenizer.characterReferenceCode = 0xFFFD
            }
            characterReferenceCode > 0x10FFFF -> {
                tokenizer.emitError(HtmlParserError.CharacterReferenceOutOfUnicodeRange())
                tokenizer.characterReferenceCode = 0xFFFD
            }
            characterReferenceCode.isSurrogate() -> {
                tokenizer.emitError(HtmlParserError.SurrogateCharacterReferenceError())
                tokenizer.characterReferenceCode = 0xFFFD
            }
            characterReferenceCode.isNonCharacter() -> {
                tokenizer.emitError(HtmlParserError.NonCharacterReferenceError())
            }
            characterReferenceCode == 0x0D || (characterReferenceCode.isControl() && !Char(characterReferenceCode).isWhitespace()) -> {
                tokenizer.emitError(HtmlParserError.ControlCharacterParseError())

                tokenizer.characterReferenceCode = when (characterReferenceCode) {
                    0x80 -> 0x20AC
                    0x82 -> 0x201A
                    0x83 -> 0x0192
                    0x84 -> 0x201E
                    0x85 -> 0x2026
                    0x86 -> 0x2020
                    0x87 -> 0x2021
                    0x88 -> 0x02C6
                    0x89 -> 0x2030
                    0x8A -> 0x0160
                    0x8B -> 0x2039
                    0x8C -> 0x0152
                    0x8E -> 0x017D
                    0x91 -> 0x2018
                    0x92 -> 0x2019
                    0x93 -> 0x201C
                    0x94 -> 0x201D
                    0x95 -> 0x2022
                    0x96 -> 0x2013
                    0x97 -> 0x2014
                    0x98 -> 0x02DC
                    0x99 -> 0x2122
                    0x9A -> 0x0161
                    0x9B -> 0x203A
                    0x9C -> 0x0153
                    0x9E -> 0x017E
                    0x9F -> 0x0178
                    else -> tokenizer.characterReferenceCode
                }
            }
        }

        tokenizer.temporaryBuffer = ""

        for (char in Character.toChars(tokenizer.characterReferenceCode)) {
            tokenizer.temporaryBuffer += char
        }

        tokenizer.flushCodePointsConsumedAsACharacterReference()
        tokenizer.switchToReturnState()
    }
}

fun Int.isC0Control(): Boolean {
    return this in 0 .. 0x001F
}

fun Int.isControl(): Boolean {
    return isC0Control() || this in 0X007F .. 0x009F
}

fun Int.isSurrogate(): Boolean {
    return this in 0xD800 .. 0xDFFF
}

fun Int.isNonCharacter(): Boolean {
    return this in 0xFDD0 .. 0xFDEF ||
        this == 0xFFFE ||
        this == 0xFFFF ||
        this == 0x1FFFE ||
        this == 0x1FFFF ||
        this == 0x2FFFE ||
        this == 0x2FFFF ||
        this == 0x3FFFE ||
        this == 0x3FFFF ||
        this == 0x4FFFE ||
        this == 0x4FFFF ||
        this == 0x5FFFE ||
        this == 0x5FFFF ||
        this == 0x6FFFE ||
        this == 0x6FFFF ||
        this == 0x7FFFE ||
        this == 0x7FFFF ||
        this == 0x8FFFE ||
        this == 0x8FFFF ||
        this == 0x9FFFE ||
        this == 0x9FFFF ||
        this == 0xAFFFE ||
        this == 0xAFFFF ||
        this == 0xBFFFE ||
        this == 0xBFFFF ||
        this == 0xCFFFE ||
        this == 0xCFFFF ||
        this == 0xDFFFE ||
        this == 0xDFFFF ||
        this == 0xEFFFE ||
        this == 0xEFFFF ||
        this == 0xFFFFE ||
        this == 0xFFFFF ||
        this == 0x10FFFE ||
        this == 0x10FFFF
}
