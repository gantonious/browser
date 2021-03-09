package ca.antonious.browser.libraries.javascript.ast

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptFunction
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.lexer.JavascriptTokenType

data class JavascriptProgram(val body: List<JavascriptStatement>)

sealed class JavascriptStatement {
    data class Block(val body: List<JavascriptStatement>) : JavascriptStatement()
    data class Function(val name: String, val parameterNames: List<String>, val body: Block) : JavascriptStatement()
    data class Return(val expression: JavascriptExpression) : JavascriptStatement()
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

    object Undefined : JavascriptValue() {
        override val isTruthy = false
        override fun toString() = "undefined"
    }

    data class Boolean(val value: kotlin.Boolean) : JavascriptValue() {
        override val isTruthy = value
        override fun toString() = value.toString()
    }

    data class Number(val value: Double) : JavascriptValue() {
        override val isTruthy = value != 0.0
        override fun toString() = value.toString()
    }

    data class String(val value: kotlin.String) : JavascriptValue() {
        override val isTruthy = value.isNotEmpty()
        override fun toString() = value
    }

    data class Object(val value: JavascriptObject) : JavascriptValue() {
        override val isTruthy = true
        override fun toString() = value.toString()
    }

    data class Function(val value: JavascriptFunction) : JavascriptValue() {
        override val isTruthy = true
        override fun toString() = value.toString()
    }
}