package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.css.CssAttribute
import ca.antonious.browser.libraries.css.CssRule
import ca.antonious.browser.libraries.css.CssSelector
import ca.antonious.browser.libraries.css.CssSize
import ca.antonious.browser.libraries.graphics.core.Color
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

        val resolvedStyle = (domParentLayoutNode.parent as? DOMParentLayoutNode)?.resolvedStyle?.copy() ?: ResolvedStyle()

        for (attribute in matchingAttributes) {
            when (attribute) {
                is CssAttribute.MarginStart -> resolvedStyle.margins.start = attribute.size
                is CssAttribute.MarginEnd -> resolvedStyle.margins.end = attribute.size
                is CssAttribute.MarginTop -> resolvedStyle.margins.top = attribute.size
                is CssAttribute.MarginBottom -> resolvedStyle.margins.bottom = attribute.size
                is CssAttribute.Width -> resolvedStyle.width = attribute.size
                is CssAttribute.FontSize -> resolvedStyle.fontSize = attribute.size
                is CssAttribute.BackgroundColor -> resolvedStyle.backgroundColor = attribute.color
                is CssAttribute.Color -> resolvedStyle.color = attribute.color
            }
        }

        return resolvedStyle
    }
}