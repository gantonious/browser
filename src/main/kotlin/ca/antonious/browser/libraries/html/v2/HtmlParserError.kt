package ca.antonious.browser.libraries.html.v2

sealed class HtmlParserError(message: String? = null) : Exception(message) {
    class EofBeforeTagName : HtmlParserError()
    class UnexpectedQuestionMarkBeforeTagName : HtmlParserError()
    class InvalidFirstCharacterOfTagName : HtmlParserError()
    class UnexpectedEqualsSignBeforeAttributeName : HtmlParserError()
    class UnexpectedCharacterInAttributeNameError : HtmlParserError()
}
