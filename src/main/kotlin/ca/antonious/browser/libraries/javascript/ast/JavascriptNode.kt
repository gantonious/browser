package ca.antonious.browser.libraries.javascript.ast

sealed class JavascriptNode {
    data class Program(val body: List<JavascriptNode>) : JavascriptNode()
    data class Return(val expression: JavascriptExpression) : JavascriptNode()
    data class Function(val name: String, val parameterNames: List<String>, val body: List<JavascriptNode>) : JavascriptNode()
}

sealed class JavascriptExpression : JavascriptNode() {
    data class FunctionCall(val name: String, val parameters: List<JavascriptExpression>) : JavascriptExpression()
    data class Reference(val name: String) : JavascriptExpression()
    data class Literal(val value: JavascriptValue) : JavascriptExpression()
    data class BooleanOperation(val operator: BooleanOperator, val lhs: JavascriptExpression, val rhs: JavascriptExpression): JavascriptExpression()
}

sealed class BooleanOperator {
    object Add : BooleanOperator()
    object Subtract : BooleanOperator()
    object Multiply : BooleanOperator()
}

sealed class JavascriptValue {
    object Undefined : JavascriptValue() {
        override fun toString(): kotlin.String {
            return "undefined"
        }
    }
    data class Double(val value: kotlin.Double) : JavascriptValue()
    data class String(val value: kotlin.String) : JavascriptValue()
}