package ca.antonious.browser.libraries.html.v2.tokenizer

interface HtmlTokenizerState {
    fun tickState(tokenizer: HtmlTokenizer)
}
