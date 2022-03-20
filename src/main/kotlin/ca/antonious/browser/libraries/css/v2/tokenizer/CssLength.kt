package ca.antonious.browser.libraries.css.v2.tokenizer

data class CssLength(val quantity: Double, val unit: Unit) {
    enum class Unit {
        px,
        em
    }
}
