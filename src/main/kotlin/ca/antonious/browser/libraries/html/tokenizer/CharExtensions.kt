package ca.antonious.browser.libraries.html.tokenizer

fun Char.isHtmlWhiteSpace(): Boolean {
    return this == '\t' ||
        this == '\n' ||
        this == '\u000C' ||
        this == ' '
}
