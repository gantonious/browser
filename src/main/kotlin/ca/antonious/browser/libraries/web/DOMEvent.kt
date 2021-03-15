package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.html.HtmlElement

sealed class DOMEvent {
    data class NodeClicked(val element: HtmlElement.Node) : DOMEvent()
}
