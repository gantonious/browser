package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.layout.core.LayoutNode

data class DOMElement(
    val htmlElement: HtmlElement,
    val layoutNode: LayoutNode,
    val resolvedStyle: ResolvedStyle
)
