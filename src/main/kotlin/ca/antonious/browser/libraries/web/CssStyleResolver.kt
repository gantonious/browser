package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.css.CssAttribute
import ca.antonious.browser.libraries.css.CssDisplay
import ca.antonious.browser.libraries.css.CssRule
import ca.antonious.browser.libraries.css.CssSelector
import ca.antonious.browser.libraries.css.CssSize
import ca.antonious.browser.libraries.graphics.core.Color
import ca.antonious.browser.libraries.web.layout.DOMParentLayoutNode

class CssStyleResolver {
    private val defaultRules = listOf(
        CssRule(
            selector = CssSelector.MatchesTag("body"),
            attributes = listOf(
                CssAttribute.MarginTop(size = CssSize.Pixel(8)),
                CssAttribute.MarginStart(size = CssSize.Pixel(8)),
                CssAttribute.MarginEnd(size = CssSize.Pixel(8)),
                CssAttribute.MarginBottom(size = CssSize.Pixel(8)),
                CssAttribute.Height(size = CssSize.Percent(1f))
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
                CssAttribute.FontSize(size = CssSize.Pixel(12))
            )
        ),
        CssRule(
            selector = CssSelector.MatchesTag("img"),
            attributes = listOf(CssAttribute.Display(displayType = CssDisplay.inlineBlock))
        )
    )

    private val rules = defaultRules.toMutableList()

    fun reset() {
        rules.clear()
        rules.addAll(defaultRules)
    }

    fun addRules(rules: List<CssRule>) {
        this.rules += rules
    }

    fun resolveStyleFor(domParentLayoutNode: DOMParentLayoutNode, inlineStyles: List<CssAttribute>): ResolvedStyle {
        val matchingAttributes = rules
            .filter { it.selector.matches(domParentLayoutNode) }
            .flatMap { it.attributes } + inlineStyles

        val resolvedStyle = domParentLayoutNode.parent?.resolvedStyle?.copy() ?: ResolvedStyle()
        resolvedStyle.displayType = CssDisplay.block
        resolvedStyle.backgroundColor = Color.clear
        resolvedStyle.width = CssSize.Percent(1f)
        resolvedStyle.height = CssSize.Auto
        resolvedStyle.margins = CssInsets.zero()

        for (attribute in matchingAttributes) {
            when (attribute) {
                is CssAttribute.MarginStart -> resolvedStyle.margins.start = attribute.size
                is CssAttribute.MarginEnd -> resolvedStyle.margins.end = attribute.size
                is CssAttribute.MarginTop -> resolvedStyle.margins.top = attribute.size
                is CssAttribute.MarginBottom -> resolvedStyle.margins.bottom = attribute.size
                is CssAttribute.Width -> resolvedStyle.width = attribute.size
                is CssAttribute.Height -> resolvedStyle.height = attribute.size
                is CssAttribute.FontSize -> resolvedStyle.fontSize = attribute.size
                is CssAttribute.BackgroundColor -> resolvedStyle.backgroundColor = attribute.color
                is CssAttribute.Color -> resolvedStyle.color = attribute.color
                is CssAttribute.TextAlignment -> resolvedStyle.textAlignment = attribute.alignment
                is CssAttribute.VerticalAlignment -> resolvedStyle.verticalAlignment = attribute.alignment
                is CssAttribute.Display -> resolvedStyle.displayType = attribute.displayType
            }
        }

        return resolvedStyle
    }
}
