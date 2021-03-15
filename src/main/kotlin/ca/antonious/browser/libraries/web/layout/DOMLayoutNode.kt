package ca.antonious.browser.libraries.web.layout

import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.layout.core.LayoutNode

abstract class DOMLayoutNode(
    val parent: DOMParentLayoutNode?,
    val htmlElement: HtmlElement
) : LayoutNode()
