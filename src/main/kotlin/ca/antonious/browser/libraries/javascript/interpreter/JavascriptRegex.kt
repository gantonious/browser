package ca.antonious.browser.libraries.javascript.interpreter

class JavascriptRegex(val regex: String, val flags: String) : JavascriptObject() {
    override fun toString(): String {
        return "/$regex/$flags"
    }
}