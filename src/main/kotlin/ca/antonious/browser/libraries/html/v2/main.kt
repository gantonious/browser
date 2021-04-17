package ca.antonious.browser.libraries.html.v2

import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlToken
import ca.antonious.browser.libraries.html.v2.tokenizer.HtmlTokenizer

fun main() {
    val html = """
        <div href=test>words 5 < 3</div>
    """.trimIndent()

    val tokens = mutableListOf<HtmlToken>()
    val tokenizer = HtmlTokenizer(html)

    var nextToken = tokenizer.nextToken()

    while (nextToken != HtmlToken.EndOfFile) {
        tokens += nextToken
        nextToken = tokenizer.nextToken()
    }

    println("[\n    ${tokens.joinToString(",\n    ")}\n]")
}
