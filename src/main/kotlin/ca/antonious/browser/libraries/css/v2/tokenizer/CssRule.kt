package ca.antonious.browser.libraries.css.v2.tokenizer

sealed class CssRule {
    data class QualifiedRule(val prelude: MutableList<ComponentValue>, var block: SimpleBlock?) : CssRule()
    data class AtRule(val prelude: MutableList<ComponentValue>, var block: SimpleBlock?) : CssRule()
}

data class SimpleBlock(
    val associatedToken: CssTokenType,
    val value: MutableList<ComponentValue>
)

sealed class ComponentValue {
    data class Block(val block: SimpleBlock) : ComponentValue()
    data class Function(val function: CssFunction) : ComponentValue()
    data class Token(val tokenType: CssTokenType) : ComponentValue()
}

data class CssFunction(
    val name: CssTokenType,
    val value: MutableList<ComponentValue>
)