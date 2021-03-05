package ca.antonious.browser.libraries.css

import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.web.DOMElement

sealed class CssSelector {
    abstract fun matches(domElement: DOMElement): Boolean

    object MatchesAny : CssSelector() {
        override fun matches(domElement: DOMElement): Boolean {
            return true
        }
    }

    data class MatchesClass(val name: String) : CssSelector() {
        override fun matches(domElement: DOMElement): Boolean {
            return when (domElement.htmlElement) {
                is HtmlElement.Node -> {
                    domElement.htmlElement.attributes.getOrDefault("class", "") == name
                }
                else -> false
            }
        }
    }

    data class MatchesTag(val tag: String) : CssSelector() {
        override fun matches(domElement: DOMElement): Boolean {
            return when (domElement.htmlElement) {
                is HtmlElement.Node -> domElement.htmlElement.name == tag
                else -> false
            }
        }
    }
}