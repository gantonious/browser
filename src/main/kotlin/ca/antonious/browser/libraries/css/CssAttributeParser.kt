package ca.antonious.browser.libraries.css

import ca.antonious.browser.libraries.graphics.core.toColor

class CssAttributeParser {

    fun parseInlineAttributes(inlineStyle: String): List<CssAttribute> {
        return inlineStyle.split(";")
            .flatMap { rawAttribute ->
                if (rawAttribute.isBlank()) {
                    return@flatMap emptyList<CssAttribute>()
                }

                val splitAttribute = rawAttribute.split(":").map { it.trim() }
                parse(attributeName = splitAttribute[0], attributeValue = splitAttribute[1])
            }
    }

    fun parse(attributeName: String, attributeValue: String): List<CssAttribute> {
        val attributes = mutableListOf<CssAttribute>()

        when (attributeName) {
            "width" -> {
                attributes += CssAttribute.Width(size = attributeValue.toCssSize())
            }
            "height" -> {
                attributes += CssAttribute.Height(size = attributeValue.toCssSize())
            }
            "margin" -> {
                val marginValues = attributeValue.trim().split(" ")
                    .map { it.trim() }
                    .map { it.toCssSize() }

                when (marginValues.size) {
                    1 -> {
                        attributes += CssAttribute.MarginTop(size = marginValues[0])
                        attributes += CssAttribute.MarginBottom(size = marginValues[0])
                        attributes += CssAttribute.MarginStart(size = marginValues[0])
                        attributes += CssAttribute.MarginEnd(size = marginValues[0])
                    }
                    2 -> {
                        attributes += CssAttribute.MarginTop(size = marginValues[0])
                        attributes += CssAttribute.MarginBottom(size = marginValues[0])
                        attributes += CssAttribute.MarginStart(size = marginValues[1])
                        attributes += CssAttribute.MarginEnd(size = marginValues[1])
                    }
                    4 -> {
                        attributes += CssAttribute.MarginTop(size = marginValues[0])
                        attributes += CssAttribute.MarginBottom(size = marginValues[1])
                        attributes += CssAttribute.MarginStart(size = marginValues[2])
                        attributes += CssAttribute.MarginEnd(size = marginValues[3])
                    }
                }
            }
            "background-color" -> {
                attributes += CssAttribute.BackgroundColor(color = attributeValue.toColor())
            }
            "background" -> {
                attributes += CssAttribute.BackgroundColor(color = attributeValue.toColor())
            }
            "color" -> {
                attributes += CssAttribute.Color(color = attributeValue.toColor())
            }
            "text-align" -> {
                attributes += CssAttribute.TextAlignment(
                    alignment = when (attributeValue.trim()) {
                        "left" -> CssHorizontalAlignment.left
                        "center" -> CssHorizontalAlignment.center
                        "right" -> CssHorizontalAlignment.right
                        else -> CssHorizontalAlignment.left
                    }
                )
            }
            "vertical-align" -> {
                attributes += CssAttribute.VerticalAlignment(
                    alignment = when (attributeValue.trim()) {
                        "top" -> CssVerticalAlignment.top
                        "middle" -> CssVerticalAlignment.middle
                        "bottom" -> CssVerticalAlignment.bottom
                        else -> CssVerticalAlignment.top
                    }
                )
            }
            "display" -> {
                attributes += CssAttribute.Display(
                    displayType = when (attributeValue.trim()) {
                        "inline",
                        "inline-block" -> CssDisplay.inlineBlock
                        "none" -> CssDisplay.none
                        else -> CssDisplay.block
                    }
                )
            }
            "position" -> {
                attributes += CssAttribute.Position(
                    positionType = when (attributeValue.trim()) {
                        "absolute" -> CssPosition.absolute
                        else -> CssPosition.static
                    }
                )
            }
            "font-size" -> {
                attributes += CssAttribute.FontSize(size = attributeValue.toCssSize())
            }
            "left" -> {
                attributes += CssAttribute.Left(size = attributeValue.toCssSize())
            }
            "right" -> {
                attributes += CssAttribute.Right(size = attributeValue.toCssSize())
            }
            "top" -> {
                attributes += CssAttribute.Top(size = attributeValue.toCssSize())
            }
            "bottom" -> {
                attributes += CssAttribute.Bottom(size = attributeValue.toCssSize())
            }
        }

        return attributes
    }
}
