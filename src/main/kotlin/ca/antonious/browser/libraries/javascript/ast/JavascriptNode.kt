package ca.antonious.browser.libraries.javascript.ast

sealed class JavascriptNode {
    data class Program(val body: List<JavascriptNode>) : JavascriptNode()
    data class Return(val expression: JavascriptExpression) : JavascriptNode()
    data class Function(val name: String, val parameterNames: List<String>, val body: List<JavascriptNode>) : JavascriptNode()
    data class IfStatement(val expression: JavascriptExpression, val body: List<JavascriptNode>) : JavascriptNode()
    data class LetAssignment(val name: String, val expression: JavascriptExpression) : JavascriptNode()
    data class WhileLoop(val condition: JavascriptExpression, val body: List<JavascriptNode>) : JavascriptNode()
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
    object LessThan : BooleanOperator()
}

sealed class JavascriptValue {
    abstract val isTruthy: kotlin.Boolean

    object Undefined : JavascriptValue() {
        override val isTruthy = false

        override fun toString(): kotlin.String {
            return "undefined"
        }
    }

    data class Boolean(val value: kotlin.Boolean) : JavascriptValue() {
        override val isTruthy = value
    }

    data class Double(val value: kotlin.Double) : JavascriptValue() {
        override val isTruthy = value != 0.0
    }

    data class String(val value: kotlin.String) : JavascriptValue() {
        override val isTruthy = value.isNotEmpty()
    }
}