package ca.antonious.browser

import ca.antonious.browser.libraries.layout.libgdx.LibgdxLayoutRunner
import ca.antonious.browser.libraries.web.WebContentLayout

fun main() {
    val webContentLayout = WebContentLayout(url = "skrundz.ca")
    LibgdxLayoutRunner().runLayout(webContentLayout)
}