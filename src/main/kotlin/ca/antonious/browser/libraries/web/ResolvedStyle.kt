package ca.antonious.browser.libraries.web

import ca.antonious.browser.libraries.graphics.core.Insets

data class ResolvedStyle(
    val margins: Insets = Insets.zero(),
    val padding: Insets = Insets.zero(),
    val width: Float? = null
)