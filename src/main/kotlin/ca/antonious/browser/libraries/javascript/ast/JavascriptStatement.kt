package ca.antonious.browser.libraries.javascript.ast

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.FunctionObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.JavascriptFunction
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction
import ca.antonious.browser.libraries.javascript.lexer.JavascriptTokenType
import ca.antonious.browser.libraries.shared.SourceInfo

data class JavascriptProgram(val body: List<JavascriptStatement>)

sealed class JavascriptStatement() {
    abstract val sourceInfo: SourceInfo

    data class LabeledStatement(override val sourceInfo: SourceInfo, val label: String, val statement: JavascriptStatement) : JavascriptStatement()
    data class Block(override val sourceInfo: SourceInfo, val body: List<JavascriptStatement>, val createsScope: Boolean = true) : JavascriptStatement()
    data class Function(override val sourceInfo: SourceInfo, val name: String, val parameterNames: List<String>, val body: Block) : JavascriptStatement()
    data class Return(override val sourceInfo: SourceInfo, val expression: JavascriptExpression?) : JavascriptStatement()
    data class IfStatement(override val sourceInfo: SourceInfo, val conditions: List<ConditionAndStatement>) : JavascriptStatement() {
        data class ConditionAndStatement(val condition: JavascriptExpression, val body: JavascriptStatement)
    }

    data class WhileLoop(override val sourceInfo: SourceInfo, val condition: JavascriptExpression, val body: JavascriptStatement?) : JavascriptStatement()
    data class DoWhileLoop(override val sourceInfo: SourceInfo, val body: JavascriptStatement, val condition: JavascriptExpression) : JavascriptStatement()
    data class LetAssignment(override val sourceInfo: SourceInfo, val assignments: List<AssignmentStatement>) : JavascriptStatement()
    data class ConstAssignment(override val sourceInfo: SourceInfo, val assignments: List<AssignmentStatement>) : JavascriptStatement()
    data class VarAssignment(override val sourceInfo: SourceInfo, val assignments: List<AssignmentStatement>) : JavascriptStatement()
    data class ForLoop(
        override val sourceInfo: SourceInfo,
        val initializerStatement: JavascriptStatement?,
        val conditionExpression: JavascriptExpression?,
        val updaterExpression: JavascriptExpression?,
        val body: JavascriptStatement?
    ) : JavascriptStatement()

    data class ForEachLoop(
        override val sourceInfo: SourceInfo,
        val initializerStatement: JavascriptStatement,
        val enumerableExpression: JavascriptExpression,
        val body: JavascriptStatement?
    ) : JavascriptStatement()

    data class TryStatement(
        override val sourceInfo: SourceInfo,
        val tryBlock: Block,
        val catchBlock: Block?,
        val errorName: String?,
        val finallyBlock: Block?
    ) : JavascriptStatement()

    data class Throw(
        override val sourceInfo: SourceInfo,
        val expression: JavascriptExpression
    ) : JavascriptStatement()

    data class Break(
        override val sourceInfo: SourceInfo,
        val label: String?
    ) : JavascriptStatement()

    data class Continue(
        override val sourceInfo: SourceInfo,
        val label: String?
    ) : JavascriptStatement()

    data class Expression(
        override val sourceInfo: SourceInfo,
        val expression: JavascriptExpression
    ) : JavascriptStatement()
}

data class AssignmentStatement(val target: AssignmentTarget, val expression: JavascriptExpression?)

sealed class AssignmentTarget {
    data class Simple(val name: String) : AssignmentTarget()

    data class ArrayDestructure(val assignmentTargets: List<DestructureTarget>) : AssignmentTarget() {
        sealed class DestructureTarget {
            object Empty : DestructureTarget()
            data class Rest(val name: String) : DestructureTarget()
            data class Single(val assignmentTarget: AssignmentTarget, val default: JavascriptExpression?) : DestructureTarget()
        }
    }
}

sealed class JavascriptExpression : JavascriptStatement() {
    data class NewCall(override val sourceInfo: SourceInfo, val function: FunctionCall) : JavascriptExpression()
    data class FunctionCall(override val sourceInfo: SourceInfo, val expression: JavascriptExpression, val parameters: List<JavascriptExpression>) :
        JavascriptExpression()

    data class DotAccess(override val sourceInfo: SourceInfo, val propertyName: String, val expression: JavascriptExpression) : JavascriptExpression()
    data class IndexAccess(override val sourceInfo: SourceInfo, val indexExpression: JavascriptExpression, val expression: JavascriptExpression) :
        JavascriptExpression()

    data class Reference(override val sourceInfo: SourceInfo, val name: String) : JavascriptExpression()

    data class ObjectLiteral(override val sourceInfo: SourceInfo, val fields: List<Field>) : JavascriptExpression() {
        sealed class Field {
            data class Value(val name: String, val rhs: JavascriptExpression) : Field()
            data class Setter(val name: String, val rhs: AnonymousFunction) : Field()
            data class Getter(val name: String, val rhs: AnonymousFunction) : Field()
            data class Spread(val expression: JavascriptExpression) : Field()
        }
    }

    data class ArrayLiteral(override val sourceInfo: SourceInfo, val items: List<JavascriptExpression>) : JavascriptExpression()

    data class RegexLiteral(override val sourceInfo: SourceInfo, val pattern: String, val flags: String) : JavascriptExpression()

    data class Literal(override val sourceInfo: SourceInfo, val value: JavascriptValue) : JavascriptExpression()
    data class TernaryOperation(
        override val sourceInfo: SourceInfo,
        val condition: JavascriptExpression,
        val ifTruthy: JavascriptExpression,
        val ifNot: JavascriptExpression
    ) : JavascriptExpression()

    data class BinaryOperation(
        override val sourceInfo: SourceInfo,
        val operator: JavascriptTokenType,
        val lhs: JavascriptExpression,
        val rhs: JavascriptExpression
    ) : JavascriptExpression()

    data class UnaryOperation(
        override val sourceInfo: SourceInfo,
        val operator: JavascriptTokenType,
        val expression: JavascriptExpression,
        val isPrefix: Boolean
    ) : JavascriptExpression()

    data class AnonymousFunction(
        override val sourceInfo: SourceInfo,
        val name: String?,
        val parameterNames: List<String>,
        val body: Block
    ) : JavascriptExpression()

    data class ArrowFunction(
        override val sourceInfo: SourceInfo,
        val parameterNames: List<String>,
        val body: JavascriptStatement
    ) : JavascriptExpression()
}

sealed class JavascriptValue {
    abstract val isTruthy: kotlin.Boolean
    abstract val typeName: kotlin.String
    abstract fun coerceToNumber(): Double
    abstract fun isSameType(other: JavascriptValue): kotlin.Boolean

    inline fun <reified T> valueAs(): T? {
        if (this !is T) return null
        return this
    }

    fun asObject(): JavascriptObject? {
        return valueAs<Object>()?.value
    }

    fun requireAsObject(): JavascriptObject {
        return asObject() ?: error("Attempted to convert $this to an object")
    }

    fun asFunction(): FunctionObject? {
        return asObject() as? FunctionObject
    }

    fun asString(): kotlin.String? {
        return valueAs<String>()?.value
    }

    fun requireAsString(): kotlin.String {
        return asString() ?: error("Attempted to convert $this to a string")
    }

    fun asNumber(): Double? {
        return valueAs<Number>()?.value
    }

    fun requireAsNumber(): Double {
        return asNumber() ?: error("Attempted to convert $this to a number")
    }

    fun toPrimitiveString(): kotlin.String {
        return if (this is JavascriptValue.Object) {
            value.interpreter.interpretAsString(this)
        } else {
            toString()
        }
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
            val undefinedOperand = lhs.valueAs<Undefined>() ?: rhs.valueAs<Undefined>()
            val nullOperand = lhs.valueAs<Null>() ?: rhs.valueAs<Null>()

            return when {
                undefinedOperand != null && nullOperand != null -> true
                undefinedOperand != null -> false
                nullOperand != null -> false
                numberOperand != null && stringOperand != null -> numberOperand.value == stringOperand.coerceToNumber()
                numberOperand != null && booleanOperand != null -> numberOperand.value == booleanOperand.coerceToNumber()
                numberOperand != null && objectOperand != null -> false
                numberOperand != null && objectOperand != null -> false
                stringOperand != null && booleanOperand != null -> stringOperand.coerceToNumber() == booleanOperand.coerceToNumber()
                stringOperand != null && objectOperand != null -> false
                booleanOperand != null && objectOperand != null -> false
                else -> error("Should never be reached")
            }
        }
    }

    object Undefined : JavascriptValue() {
        override val isTruthy = false
        override val typeName = "undefined"
        override fun toString() = "undefined"
        override fun coerceToNumber() = Double.NaN
        override fun isSameType(other: JavascriptValue) = other is Undefined
    }

    object Null : JavascriptValue() {
        override val isTruthy = false
        override val typeName = "object"
        override fun toString() = "null"
        override fun coerceToNumber() = 0.0
        override fun isSameType(other: JavascriptValue) = other is Null
    }

    data class Boolean(val value: kotlin.Boolean) : JavascriptValue() {
        override val isTruthy = value
        override val typeName = "boolean"
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
        override val typeName = "number"
        override fun toString() = if (value == value.toInt().toDouble()) {
            value.toInt().toString()
        } else {
            value.toString()
        }

        override fun coerceToNumber() = value
        override fun isSameType(other: JavascriptValue) = other is Number
    }

    data class String(val value: kotlin.String) : JavascriptValue() {
        override val isTruthy = value.isNotEmpty()
        override val typeName = "string"
        override fun toString() = value
        override fun coerceToNumber() = value.toDoubleOrNull() ?: Double.NaN
        override fun isSameType(other: JavascriptValue) = other is String
    }

    data class Object(val value: JavascriptObject) : JavascriptValue() {
        override val isTruthy = true
        override fun toString() = value.toString()
        override fun coerceToNumber() = Double.NaN
        override fun isSameType(other: JavascriptValue) = other is Object

        override val typeName = when (value) {
            is JavascriptFunction, is NativeFunction -> "function"
            else -> "object"
        }
    }
}
