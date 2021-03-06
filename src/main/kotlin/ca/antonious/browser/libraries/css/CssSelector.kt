package ca.antonious.browser.libraries.css

import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.web.DOMElement
import ca.antonious.browser.libraries.web.layout.DOMParentLayoutNode

sealed class CssSelector {
    abstract fun matches(domElement: DOMParentLayoutNode): Boolean

    object MatchesAny : CssSelector() {
        override fun matches(domElement: DOMParentLayoutNode): Boolean {
            return true
        }
    }

    data class MatchesClass(val name: String) : CssSelector() {
        override fun matches(domElement: DOMParentLayoutNode): Boolean {
            return when (domElement.htmlElement) {
                is HtmlElement.Node -> {
                    domElement.htmlElement.attributes.getOrDefault("class", "") == name
                }
                else -> false
            }
        }
    }

    data class MatchesTag(val tag: String) : CssSelector() {
        override fun matches(domElement: DOMParentLayoutNode): Boolean {
            return when (domElement.htmlElement) {
                is HtmlElement.Node -> domElement.htmlElement.name == tag
                else -> false
            }
        }
    }

    data class MatchesParent(val parentSelectors: List<CssSelector>) : CssSelector() {
        override fun matches(domElement: DOMParentLayoutNode): Boolean {
            return false
        }
    }

    data class MatchesState(val selector: CssSelector, val requiredState: String) : CssSelector() {
        override fun matches(domElement: DOMParentLayoutNode): Boolean {
            return false
        }
    }
}