package ca.antonious.browser

import ca.antonious.browser.libraries.graphics.core.toColor
import ca.antonious.browser.libraries.layout.libgdx.LibgdxLayoutRunner
import ca.antonious.browser.libraries.web.WebContentLayout

fun main() {
    val webContentLayout = WebContentLayout(url = "localhost:8081")
    LibgdxLayoutRunner().runLayout(webContentLayout)
}