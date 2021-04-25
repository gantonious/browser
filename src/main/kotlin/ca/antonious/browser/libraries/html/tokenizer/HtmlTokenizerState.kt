package ca.antonious.browser.libraries.html.tokenizer

interface HtmlTokenizerState {
    fun tickState(tokenizer: HtmlTokenizer)
}
