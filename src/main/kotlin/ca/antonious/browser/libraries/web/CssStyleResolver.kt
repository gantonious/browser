package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.css.CssAttribute
import ca.antonious.browser.libraries.css.CssRule
import ca.antonious.browser.libraries.css.CssSelector
import ca.antonious.browser.libraries.css.CssSize
import ca.antonious.browser.libraries.graphics.core.Insets
import ca.antonious.browser.libraries.web.layout.DOMParentLayoutNode

class CssStyleResolver {
    private val rules = mutableListOf(
        CssRule(
            selector = CssSelector.MatchesTag("body"),
            attributes = listOf(
                CssAttribute.MarginTop(size = CssSize.Pixel(8)),
                CssAttribute.MarginStart(size = CssSize.Pixel(8)),
                CssAttribute.MarginEnd(size = CssSize.Pixel(8)),
                CssAttribute.MarginBottom(size = CssSize.Pixel(8))
            )
        ),
        CssRule(
            selector = CssSelector.MatchesTag("h1"),
            attributes = listOf(
                CssAttribute.MarginTop(size = CssSize.Pixel(8)),
                CssAttribute.MarginStart(size = CssSize.Pixel(8)),
                CssAttribute.MarginEnd(size = CssSize.Pixel(8)),
                CssAttribute.MarginBottom(size = CssSize.Pixel(8)),
                CssAttribute.FontSize(size = CssSize.Pixel(36))
            )
        ),
        CssRule(
            selector = CssSelector.MatchesTag("p"),
            attributes = listOf(
                CssAttribute.MarginTop(size = CssSize.Pixel(8)),
                CssAttribute.MarginStart(size = CssSize.Pixel(8)),
                CssAttribute.MarginEnd(size = CssSize.Pixel(8)),
                CssAttribute.MarginBottom(size = CssSize.Pixel(8)),
                CssAttribute.FontSize(size = CssSize.Pixel(18))
            )
        )
    )

    fun addRules(rules: List<CssRule>) {
        this.rules += rules
    }

    fun resolveStyleFor(domParentLayoutNode: DOMParentLayoutNode): ResolvedStyle {
        val matchingAttributes = rules
            .filter { it.selector.matches(domParentLayoutNode) }
            .flatMap { it.attributes }

        val margins = CssInsets.zero()
        val padding = CssInsets.zero()
        var width: CssSize = CssSize.Auto
        var fontSize: CssSize = CssSize.Pixel(8)

        for (attribute in matchingAttributes) {
            when (attribute) {
                is CssAttribute.MarginStart -> margins.start = attribute.size
                is CssAttribute.MarginEnd -> margins.end = attribute.size
                is CssAttribute.MarginTop -> margins.top = attribute.size
                is CssAttribute.MarginBottom -> margins.bottom = attribute.size
                is CssAttribute.Width -> width = attribute.size
                is CssAttribute.FontSize -> fontSize = attribute.size
            }
        }

        return ResolvedStyle(
            margins = margins,
            padding = padding,
            width = width,
            fontSize = fontSize
        )
    }
}