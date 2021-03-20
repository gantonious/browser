package ca.antonious.browser

import ca.antonious.browser.libraries.layout.libgdx.LibgdxLayoutRunner
import ca.antonious.browser.libraries.web.WebContentLayout

fun main(args: Array<String>) {
    val url = args.firstOrNull() ?: error("Expected url to be passed as arg.")
    val webContentLayout = WebContentLayout(url = url)
    LibgdxLayoutRunner().runLayout(webContentLayout)
}
