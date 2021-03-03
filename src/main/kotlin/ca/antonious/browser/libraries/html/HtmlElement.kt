package ca.antonious.browser.libraries.html

sealed class HtmlElement {
    data class Node(
        val name: String,
        val attributes: Map<String, String> = emptyMap(),
        val children: List<HtmlElement> = emptyList()
    ) : HtmlElement() {
        fun requireChildrenAsText(): Text {
            return children.first() as Text
        }
    }

    data class Text(
        val text: String
    ) : HtmlElement()
}