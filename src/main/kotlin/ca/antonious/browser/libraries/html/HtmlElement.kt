package ca.antonious.browser.libraries.html

sealed class HtmlElement {
    data class Node(
        val name: String,
        var attributes: MutableMap<String, String> = mutableMapOf(),
        var children: MutableList<HtmlElement> = mutableListOf()
    ) : HtmlElement() {
        fun requireChildrenAsText(): Text {
            return children.firstOrNull()?.let { it as Text } ?: Text("")
        }
    }

    data class Text(
        var text: String
    ) : HtmlElement()

    fun requireAsText(): Text {
        return this as Text
    }
}
