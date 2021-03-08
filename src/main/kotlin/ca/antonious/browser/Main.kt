package ca.antonious.browser

import ca.antonious.browser.libraries.layout.libgdx.LibgdxLayoutRunner
import ca.antonious.browser.libraries.web.WebContentLayout

fun main() {
    val webContentLayout = WebContentLayout(url = "https://www.antonious.ca/")
    LibgdxLayoutRunner().runLayout(webContentLayout)
}