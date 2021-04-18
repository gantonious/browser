package ca.antonious.browser.libraries.html.v2.tokenizer

sealed class HtmlToken {
    data class Doctype(var name: String = "", var forceQuirks: Boolean = false) : HtmlToken()
    data class Character(val char: Char) : HtmlToken()
    data class Comment(var comment: String = "") : HtmlToken()
    abstract class Tag(var name: String = "") : HtmlToken()

    class StartTag(
        var selfClosing: Boolean = false,
        var selfClosingAcknowledged: Boolean = false,
        val attributes: MutableList<Attribute> = mutableListOf(),
        name: String = ""
    ) : Tag(name) {
        val currentAttribute: Attribute
            get() = attributes.last()

        fun addEmptyAttribute() {
            attributes.add(Attribute())
        }

        fun acknowledgeSelfClosingIfSet() {
            if (selfClosing) {
                selfClosingAcknowledged = true
            }
        }

        override fun toString(): String {
            return "StartTag(name=$name, selfClosing=$selfClosing, selfClosingAcknowledged=$selfClosingAcknowledged, attributes=$attributes)"
        }

        data class Attribute(var name: String = "", var value: String = "")


    }
    class EndTag : Tag()
    object EndOfFile : HtmlToken()
}
