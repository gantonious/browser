package ca.antonious.browser.libraries.html.v2

sealed class HtmlToken {
    data class Character(val char: Char) : HtmlToken()
    data class Comment(val comment: String) : HtmlToken()
    abstract class Tag : HtmlToken() {
        var name: String = ""
    }
    data class StartTag(
        var selfClosing: Boolean = false,
        val attributes: MutableList<Attribute> = mutableListOf()
    ) : Tag() {
        val currentAttribute: Attribute
            get() = attributes.last()

        fun addEmptyAttribute() {
            attributes.add(Attribute())
        }

        data class Attribute(var name: String = "", var value: String = "")
    }
    class EndTag : Tag()
    object EndOfFile : HtmlToken()
}
