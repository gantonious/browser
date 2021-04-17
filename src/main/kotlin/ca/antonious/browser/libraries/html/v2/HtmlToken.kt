package ca.antonious.browser.libraries.html.v2

sealed class HtmlToken {
    data class Character(val char: Char) : HtmlToken()
    data class Comment(val comment: String) : HtmlToken()
    data class StartTag(
        var name: String = "",
        var currentAttributeName: String = "",
        var currentAttributeValue: String = ""
    ) : HtmlToken()
    object EndOfFile : HtmlToken()
}
