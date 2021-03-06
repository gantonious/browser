package ca.antonious.browser.libraries.html

sealed class HtmlElement {
    data class Node(
        val name: String,
        val attributes: Map<String, String> = emptyMap(),
        val children: List<HtmlElement> = emptyList()
    ) : HtmlElement() {
        fun requireChildrenAsText(): Text {
            return children.firstOrNull()?.let { it as Text } ?: Text("")
        }
    }

    data class Text(
        val text: String
    ) : HtmlElement()

    fun requireAsText(): Text {
        return this as Text
    }
}