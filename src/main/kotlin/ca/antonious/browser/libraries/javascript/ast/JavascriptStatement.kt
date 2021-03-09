package ca.antonious.browser.libraries.javascript.ast

import ca.antonious.browser.libraries.javascript.interpreter.builtins.JavascriptFunction
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.lexer.JavascriptTokenType

data class JavascriptProgram(val body: List<JavascriptStatement>)

sealed class JavascriptStatement {
    data class Block(val body: List<JavascriptStatement>) : JavascriptStatement()
    data class Function(val name: String, val parameterNames: List<String>, val body: Block) : JavascriptStatement()
    data class Return(val expression: JavascriptExpression?) : JavascriptStatement()
    data class IfStatement(val conditions: List<ConditionAndBlock>) : JavascriptStatement() {
        data class ConditionAndBlock(val condition: JavascriptExpression, val body: Block)
    }
    data class WhileLoop(val condition: JavascriptExpression, val body: Block) : JavascriptStatement()
    data class LetAssignment(val name: String, val expression: JavascriptExpression) : JavascriptStatement()
    data class ConstAssignment(val name: String, val expression: JavascriptExpression) : JavascriptStatement()
    data class ForLoop(
        val initializerExpression: JavascriptExpression,
        val conditionExpression: JavascriptExpression,
        val updaterExpression: JavascriptExpression,
        val body: Block
    ) : JavascriptStatement()
}

sealed class JavascriptExpression : JavascriptStatement() {
    data class FunctionCall(val expression: JavascriptExpression, val parameters: List<JavascriptExpression>) : JavascriptExpression()
    data class DotAccess(val propertyName: String, val expression: JavascriptExpression) : JavascriptExpression()
    data class IndexAccess(val indexExpression: JavascriptExpression, val expression: JavascriptExpression) : JavascriptExpression()
    data class Reference(val name: String) : JavascriptExpression()
    data class Literal(val value: JavascriptValue) : JavascriptExpression()
    data class BinaryOperation(val operator: JavascriptTokenType.Operator, val lhs: JavascriptExpression, val rhs: JavascriptExpression): JavascriptExpression()
    data class UnaryOperation(val operator: JavascriptTokenType, val expression: JavascriptExpression, val isPrefix: Boolean): JavascriptExpression()
    data class AnonymousFunction(val parameterNames: List<String>, val body: Block) : JavascriptExpression()
}

sealed class JavascriptValue {
    abstract val isTruthy: kotlin.Boolean
    abstract fun coerceToNumber(): Double
    abstract fun isSameType(other: JavascriptValue): kotlin.Boolean

    inline fun <reified T> valueAs(): T? {
        if (this !is T) return null
        return this
    }

    companion object {
        fun looselyEquals(lhs: JavascriptValue, rhs: JavascriptValue): kotlin.Boolean {
            if (lhs.isSameType(rhs)) {
                return lhs == rhs
            }

            val booleanOperand = lhs.valueAs<Boolean>() ?: rhs.valueAs<Boolean>()
            val stringOperand = lhs.valueAs<String>() ?: rhs.valueAs<String>()
            val numberOperand = lhs.valueAs<Number>() ?: rhs.valueAs<Number>()
            val objectOperand = lhs.valueAs<Object>() ?: rhs.valueAs<Object>()
            val functionOperand = lhs.valueAs<Object>() ?: rhs.valueAs<Object>()
            val undefinedOperand = lhs.valueAs<Undefined>() ?: rhs.valueAs<Undefined>()

            return when {
                undefinedOperand != null -> false
                numberOperand != null && stringOperand != null -> numberOperand.value == stringOperand.coerceToNumber()
                numberOperand != null && booleanOperand != null -> numberOperand.value == booleanOperand.coerceToNumber()
                numberOperand != null && objectOperand != null -> false
                numberOperand != null && objectOperand != null -> false
                stringOperand != null && booleanOperand != null -> stringOperand.coerceToNumber() == booleanOperand.coerceToNumber()
                stringOperand != null && objectOperand != null  -> false
                stringOperand != null && functionOperand != null  -> false
                booleanOperand != null && objectOperand != null -> false
                booleanOperand != null && functionOperand != null -> false
                objectOperand != null && functionOperand != null -> false
                else -> error("Should never be reached")
            }
        }
    }

    object Undefined : JavascriptValue() {
        override val isTruthy = false
        override fun toString() = "undefined"
        override fun coerceToNumber() = Double.NaN
        override fun isSameType(other: JavascriptValue) = other is Undefined
    }

    data class Boolean(val value: kotlin.Boolean) : JavascriptValue() {
        override val isTruthy = value
        override fun toString() = value.toString()
        override fun isSameType(other: JavascriptValue) = other is Boolean

        override fun coerceToNumber(): Double {
            return if (value) {
                1.0
            } else {
                0.0
            }
        }
    }

    data class Number(val value: Double) : JavascriptValue() {
        override val isTruthy = value != 0.0
        override fun toString() = value.toString()
        override fun coerceToNumber() = value
        override fun isSameType(other: JavascriptValue) = other is Number
    }

    data class String(val value: kotlin.String) : JavascriptValue() {
        override val isTruthy = value.isNotEmpty()
        override fun toString() = value
        override fun coerceToNumber() = value.toDoubleOrNull() ?: Double.NaN
        override fun isSameType(other: JavascriptValue) = other is String
    }

    data class Object(val value: JavascriptObject) : JavascriptValue() {
        override val isTruthy = true
        override fun toString() = value.toString()
        override fun coerceToNumber() = Double.NaN
        override fun isSameType(other: JavascriptValue) = other is Object
    }

    data class Function(val value: JavascriptFunction) : JavascriptValue() {
        override val isTruthy = true
        override fun toString() = value.toString()
        override fun coerceToNumber() = Double.NaN
        override fun isSameType(other: JavascriptValue) = other is Function
    }
}