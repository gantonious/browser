package ca.antonious.browser.libraries.javascript.lexer

sealed class JavascriptTokenType {
    data class Identifier(val name: kotlin.String) : JavascriptTokenType()
    object Dot : JavascriptTokenType()
    object Comma : JavascriptTokenType()
    object Colon : JavascriptTokenType()
    object SemiColon : JavascriptTokenType()
    object QuestionMark : JavascriptTokenType()
    object OpenParentheses : JavascriptTokenType()
    object CloseParentheses : JavascriptTokenType()
    object OpenCurlyBracket : JavascriptTokenType()
    object CloseCurlyBracket : JavascriptTokenType()
    object OpenBracket : JavascriptTokenType()
    object CloseBracket : JavascriptTokenType()
    object BitNot: JavascriptTokenType()
    object Not : JavascriptTokenType()
    object OrAssign : JavascriptTokenType()
    object AndAssign : JavascriptTokenType()
    object XorAssign : JavascriptTokenType()
    object ModAssign : JavascriptTokenType()
    object PlusAssign : JavascriptTokenType()
    object MinusAssign : JavascriptTokenType()
    object MultiplyAssign : JavascriptTokenType()
    object DivideAssign : JavascriptTokenType()
    object Assignment : JavascriptTokenType()
    object Function : JavascriptTokenType()
    object While : JavascriptTokenType()
    object If : JavascriptTokenType()
    object Return : JavascriptTokenType()
    object Let : JavascriptTokenType()
    object Const : JavascriptTokenType()

    data class String(val value: kotlin.String) : JavascriptTokenType()
    data class Number(val value: Double) : JavascriptTokenType()
    data class Boolean(val value: kotlin.Boolean) : JavascriptTokenType()
    object Undefined : JavascriptTokenType()

    sealed class Operator : JavascriptTokenType() {
        object Plus : Operator()
        object Minus : Operator()
        object Multiply : Operator()
        object Divide : Operator()
        object Or : Operator()
        object And: Operator()
        object AndAnd : Operator()
        object OrOr : Operator()
        object Mod : Operator()
        object Xor : Operator()
        object LessThan : Operator()
        object LessThanOrEqual : Operator()
        object GreaterThan : Operator()
        object GreaterThanOrEqual : Operator()
    }
}

data class JavascriptToken(
    val type: JavascriptTokenType,
    val sourceInfo: SourceInfo
)

data class SourceInfo(val line: Int, val column: Int)