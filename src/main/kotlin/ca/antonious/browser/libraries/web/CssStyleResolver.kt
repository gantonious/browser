package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.css.CssAttribute
import ca.antonious.browser.libraries.css.CssRule
import ca.antonious.browser.libraries.css.CssSelector
import ca.antonious.browser.libraries.css.CssSize
import ca.antonious.browser.libraries.graphics.core.Insets
import ca.antonious.browser.libraries.graphics.core.Rect

class CssStyleResolver {
    private val rules = mutableListOf(
        CssRule(
            selector = CssSelector.MatchesAny,
            attributes = listOf(
                CssAttribute.Margin(size = CssSize.Pixel(8))
            )
        )
    )

    fun addRules(rules: List<CssRule>) {
        this.rules += rules
    }

    fun resolveStyleFor(domElement: DOMElement): ResolvedStyle {
        val matchingAttributes = rules
            .filter { it.selector.matches(domElement) }
            .flatMap { it.attributes }

        val margins = Insets.zero()
        val padding = Insets.zero()
        var width: Float? = null

        for (attribute in matchingAttributes) {
            when (attribute) {
                is CssAttribute.Margin -> {
                    attribute.size.toFloat()?.let {
                        margins.start = it
                        margins.end = it
                        margins.top = it
                        margins.bottom = it
                    }
                }
                is CssAttribute.Width -> {
                    width = attribute.size.toFloat()
                }
            }
        }

        return ResolvedStyle(
            margins = margins,
            padding = padding,
            width = width
        )
    }
}