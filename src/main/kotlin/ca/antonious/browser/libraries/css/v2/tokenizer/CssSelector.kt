package ca.antonious.browser.libraries.css.v2.tokenizer

sealed class CssSelector {
    data class Simple(val simpleSelector: SimpleSelector) : CssSelector()
    data class Compound(val simpleSelectors: List<SimpleSelector>) : CssSelector()
    data class Complex(val rootSelector: Compound, val combinedSelectors: List<CombinedSelector>) : CssSelector()
}

data class CombinedSelector(
    val combinator: SelectorCombinator,
    val selector: CssSelector
)

enum class SelectorCombinator {
    Descendant,
    Child,
    NextSibling,
    SubsequentSibling
}

sealed class SimpleSelector {
    data class Type(val ident: CssTokenType.Ident) : SimpleSelector()
    data class Class(val ident: CssTokenType.Ident) : SimpleSelector()
    data class Id(val hash: CssTokenType.HashToken) : SimpleSelector()
    data class Attribute(val id: String) : SimpleSelector()
}
