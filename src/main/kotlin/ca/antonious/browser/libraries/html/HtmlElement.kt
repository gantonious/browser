package ca.antonious.browser.libraries.html

sealed class HtmlElement {
    data class Node(
        val name: String,
        val attributes: Map<String, String> = emptyMap(),
        val children: List<HtmlElement>
    ) : HtmlElement()

    data class Text(
        val text: String
    ) : HtmlElement()
}
val sampleHtml = listOf(
    HtmlElement.Node(
        name = "head",
        children = listOf(
            HtmlElement.Node(
                name = "title",
                children = listOf(
                    HtmlElement.Text("Title")
                )
            )
        )
    ),
    HtmlElement.Node(
        name = "body",
        children = listOf(
            HtmlElement.Node(
                name = "h1",
                children = listOf(
                    HtmlElement.Text("Heading")
                )
            )
        )
    )
)