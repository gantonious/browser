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
    object Function : JavascriptTokenType()
    object While : JavascriptTokenType()
    object For : JavascriptTokenType()
    object If : JavascriptTokenType()
    object Return : JavascriptTokenType()
    object Let : JavascriptTokenType()
    object Const : JavascriptTokenType()
    object PlusPlus : JavascriptTokenType()
    object MinusMinus : JavascriptTokenType()

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
        object OrAssign : Operator()
        object AndAssign : Operator()
        object XorAssign : Operator()
        object ModAssign : Operator()
        object PlusAssign : Operator()
        object MinusAssign : Operator()
        object MultiplyAssign : Operator()
        object DivideAssign : Operator()
        object Assignment : Operator()
    }
}

data class JavascriptToken(
    val type: JavascriptTokenType,
    val sourceInfo: SourceInfo
)

data class SourceInfo(val line: Int, val column: Int)