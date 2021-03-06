package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.css.CssAttribute
import ca.antonious.browser.libraries.css.CssDisplay
import ca.antonious.browser.libraries.css.CssPosition
import ca.antonious.browser.libraries.css.CssRule
import ca.antonious.browser.libraries.css.CssSelector
import ca.antonious.browser.libraries.css.CssSize
import ca.antonious.browser.libraries.css.toCssSize
import ca.antonious.browser.libraries.graphics.core.Color
import ca.antonious.browser.libraries.web.layout.DOMElementNode

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
        ),
        CssRule(
            selector = CssSelector.MatchesTag("br"),
            attributes = listOf(CssAttribute.Height(size = CssSize.Em(1)))
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

    fun resolveStyleFor(domElementNode: DOMElementNode, inlineStyles: List<CssAttribute>): ResolvedStyle {
        val matchingAttributes = rules
            .filter { it.selector.matches(domElementNode) }
            .flatMap { it.attributes } + inlineStyles

        val resolvedStyle = domElementNode.parent?.resolvedStyle?.copy() ?: ResolvedStyle()
        resolvedStyle.displayType = CssDisplay.block
        resolvedStyle.positionType = CssPosition.static
        resolvedStyle.backgroundColor = Color.clear
        resolvedStyle.width = CssSize.Percent(1f)
        resolvedStyle.height = CssSize.Auto
        resolvedStyle.margins = CssInsets.zero()
        resolvedStyle.left = CssSize.Auto
        resolvedStyle.right = CssSize.Auto
        resolvedStyle.top = CssSize.Auto
        resolvedStyle.bottom = CssSize.Auto

        var hasModifiedWidth = false

        for (attribute in matchingAttributes) {
            when (attribute) {
                is CssAttribute.MarginStart -> resolvedStyle.margins.start = attribute.size
                is CssAttribute.MarginEnd -> resolvedStyle.margins.end = attribute.size
                is CssAttribute.MarginTop -> resolvedStyle.margins.top = attribute.size
                is CssAttribute.MarginBottom -> resolvedStyle.margins.bottom = attribute.size
                is CssAttribute.Width -> {
                    resolvedStyle.width = attribute.size
                    hasModifiedWidth = true
                }
                is CssAttribute.Height -> resolvedStyle.height = attribute.size
                is CssAttribute.FontSize -> resolvedStyle.fontSize = attribute.size
                is CssAttribute.BackgroundColor -> resolvedStyle.backgroundColor = attribute.color
                is CssAttribute.Color -> resolvedStyle.color = attribute.color
                is CssAttribute.TextAlignment -> resolvedStyle.textAlignment = attribute.alignment
                is CssAttribute.VerticalAlignment -> resolvedStyle.verticalAlignment = attribute.alignment
                is CssAttribute.Display -> resolvedStyle.displayType = attribute.displayType
                is CssAttribute.Position -> resolvedStyle.positionType = attribute.positionType
                is CssAttribute.Top -> resolvedStyle.top = attribute.size
                is CssAttribute.Bottom -> resolvedStyle.bottom = attribute.size
                is CssAttribute.Left -> resolvedStyle.left = attribute.size
                is CssAttribute.Right -> resolvedStyle.right = attribute.size
            }
        }

        domElementNode.htmlNode.attributes["width"]?.let {
            resolvedStyle.width = it.toCssSize()
        }

        domElementNode.htmlNode.attributes["height"]?.let {
            resolvedStyle.height = it.toCssSize()
        }

        if (!hasModifiedWidth && resolvedStyle.positionType == CssPosition.absolute) {
            resolvedStyle.width = CssSize.Auto
        }

        return resolvedStyle
    }
}
