package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.*
import ca.antonious.browser.libraries.javascript.interpreter.builtins.*
import ca.antonious.browser.libraries.javascript.lexer.JavascriptLexer
import ca.antonious.browser.libraries.javascript.lexer.JavascriptTokenType
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser
import java.util.*
import kotlin.random.Random

class JavascriptInterpreter {
    val globalObject = JavascriptObject().apply {
        setNonEnumerableNativeFunction("getInput") { executionContext ->
            val inputText = executionContext.arguments.firstOrNull() as? JavascriptValue.String
            if (inputText != null) {
                print(inputText.value)
            }
            JavascriptValue.Number((readLine() ?: "").toDouble())
        }

        setNonEnumerableNativeFunction("parseInt") { executionContext ->
            JavascriptValue.Number(executionContext.arguments.first().coerceToNumber().toInt().toDouble())
        }

        setProperty("console", JavascriptValue.Object(JavascriptObject().apply {
            setNonEnumerableNativeFunction("log") { executionContext ->
                println(executionContext.arguments.joinToString(separator = " "))
                JavascriptValue.Undefined
            }
        }))

        setProperty("Math", JavascriptValue.Object(JavascriptObject().apply {
            setNonEnumerableNativeFunction("floor") { executionContext ->
                JavascriptValue.Number(executionContext.arguments.first().coerceToNumber().toInt().toDouble())
            }

            setNonEnumerableNativeFunction("random") {
                JavascriptValue.Number(Random.nextDouble())
            }
        }))
    }

    private var stack = Stack<JavascriptStackFrame>().apply {
        push(
            JavascriptStackFrame(
                scope = JavascriptScope(
                    thisBinding = globalObject,
                    scopeObject = JavascriptObject(),
                    parentScope = null
                )
            )
        )
    }

    private var controlFlowInterruption: ControlFlowInterruption? = null
    private var lastReturn: JavascriptReference? = null

    private val currentScope: JavascriptScope
        get() = stack.peek().scope


    fun interpret(javascript: String): JavascriptValue {
        val tokens = JavascriptLexer(javascript).lex()
        val program = JavascriptParser(tokens, javascript).parse()
        return interpret(program)
    }

    fun interpret(program: JavascriptProgram): JavascriptValue {
        return interpret(JavascriptStatement.Block(program.body))
    }

    private fun interpret(statement: JavascriptStatement): JavascriptValue {
        return interpretAsReference(statement).value
    }

    private fun interpretAsReference(statement: JavascriptStatement): JavascriptReference {
        when (statement) {
            is JavascriptStatement.LabeledStatement -> {
                return interpretAsReference(statement.statement)
            }
            is JavascriptStatement.Block -> {
                var result: JavascriptReference = JavascriptReference.Undefined
                for (child in statement.body) {
                    result = interpretAsReference(child)
                    if (hasControlFlowInterrupted()) {
                        break
                    }
                }

                return result
            }
            is JavascriptStatement.Function -> {
                val value = JavascriptValue.Object(
                    value = JavascriptFunction(
                        parameterNames = statement.parameterNames,
                        body = statement.body,
                        parentScope = currentScope
                    )
                )
                currentScope.setProperty(key = statement.name, value = value)
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.Return -> {
                val value = if (statement.expression != null) {
                    interpret(statement.expression)
                } else {
                    JavascriptValue.Undefined
                }

                interruptControlFlowWith(ControlFlowInterruption.Return(value))

                return JavascriptReference.Undefined
            }
            is JavascriptExpression.FunctionCall -> {
                val (valueToCall, thisBinding) = when (statement.expression) {
                    is JavascriptExpression.DotAccess -> {
                        val objectToInvoke = (interpret(statement.expression.expression) as JavascriptValue.Object).value
                        objectToInvoke.getProperty(statement.expression.propertyName) to objectToInvoke
                    }
                    else -> {
                        interpret(statement.expression) to currentScope.thisBinding
                    }
                }

                return when (val objectToCall = (valueToCall as JavascriptValue.Object).value) {
                    is JavascriptFunction -> {
                        enterFunction(
                            parameterNames = objectToCall.parameterNames,
                            passedParameters = statement.parameters,
                            parentScope = objectToCall.parentScope,
                            thisBinding = thisBinding
                        )

                        interpret(objectToCall.body)

                        exitFunction()

                        (maybeConsumeControlFlowInterrupt<ControlFlowInterruption.Return>()?.value ?: JavascriptValue.Undefined)
                    }
                    is NativeFunction -> {
                        val nativeExecutionContext = NativeExecutionContext(
                            arguments = statement.parameters.map { interpret(it) },
                            thisBinding = thisBinding,
                            interpreter = this
                        )

                        objectToCall.body.invoke(nativeExecutionContext)
                    }
                    else -> error("TypeError: $objectToCall is not a function")
                }.toReference()
            }
            is JavascriptStatement.IfStatement -> {
                val conditionToExecute = statement.conditions.firstOrNull {
                    interpret(it.condition).isTruthy
                }
                if (conditionToExecute != null) {
                    interpret(conditionToExecute.body)
                }
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.LetAssignment -> {
                currentScope.setProperty(statement.name, interpret(statement.expression))
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.ConstAssignment -> {
                currentScope.setProperty(statement.name, interpret(statement.expression))
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.VarAssignment -> {
                currentScope.setProperty(statement.name, interpret(statement.expression))
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.WhileLoop -> {
                while (!hasControlFlowInterrupted() && interpret(statement.condition).isTruthy) {
                    interpret(statement.body)
                }
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.DoWhileLoop -> {
                do {
                    interpret(statement.body)
                } while (!hasControlFlowInterrupted() && interpret(statement.condition).isTruthy)

                return JavascriptReference.Undefined
            }
            is JavascriptStatement.ForLoop -> {
                interpret(statement.initializerStatement)

                while (!hasControlFlowInterrupted() && interpret(statement.conditionExpression).isTruthy) {
                    interpret(statement.body)
                    statement.updaterExpression?.let { interpret(it) }
                }

                return JavascriptReference.Undefined
            }
            is JavascriptExpression.TernaryOperation -> {
                return if (interpret(statement.condition).isTruthy) {
                    interpret(statement.ifTruthy)
                } else {
                    interpret(statement.ifNot)
                }.toReference()
            }
            is JavascriptExpression.BinaryOperation -> {
                return when (statement.operator) {
                    is JavascriptTokenType.Operator.Plus -> {
                        val lhsValue = interpretAsReference(statement.lhs).value
                        val rhsValue = interpretAsReference(statement.rhs).value

                        if (lhsValue is JavascriptValue.String || rhsValue is JavascriptValue.String) {
                            JavascriptValue.String(lhsValue.toString() + rhsValue.toString()).toReference()
                        } else {
                            JavascriptValue.Number(lhsValue.coerceToNumber() + rhsValue.coerceToNumber()).toReference()
                        }
                    }
                    is JavascriptTokenType.Operator.Minus -> {
                        JavascriptValue.Number(interpret(statement.lhs).coerceToNumber() - interpret(statement.rhs).coerceToNumber()).toReference()
                    }
                    is JavascriptTokenType.Operator.Multiply -> {
                        JavascriptValue.Number(interpret(statement.lhs).coerceToNumber() * interpret(statement.rhs).coerceToNumber()).toReference()
                    }
                    is JavascriptTokenType.Operator.Divide -> {
                        JavascriptValue.Number(interpret(statement.lhs).coerceToNumber() / interpret(statement.rhs).coerceToNumber()).toReference()
                    }
                    is JavascriptTokenType.Operator.Xor -> {
                        val result = (
                            interpret(statement.lhs).coerceToNumber().toInt() xor
                                interpret(statement.rhs).coerceToNumber().toInt()
                            ).toDouble()

                        JavascriptValue.Number(result).toReference()
                    }
                    is JavascriptTokenType.Operator.Mod -> {
                        JavascriptValue.Number(interpret(statement.lhs).coerceToNumber() % interpret(statement.rhs).coerceToNumber()).toReference()
                    }
                    is JavascriptTokenType.Operator.LessThanOrEqual -> {
                        JavascriptValue.Boolean(interpret(statement.lhs).coerceToNumber() <= interpret(statement.rhs).coerceToNumber()).toReference()
                    }
                    is JavascriptTokenType.Operator.LessThan -> {
                        JavascriptValue.Boolean(interpret(statement.lhs).coerceToNumber() < interpret(statement.rhs).coerceToNumber()).toReference()
                    }
                    is JavascriptTokenType.Operator.GreaterThanOrEqual -> {
                        JavascriptValue.Boolean(interpret(statement.lhs).coerceToNumber() >= interpret(statement.rhs).coerceToNumber()).toReference()
                    }
                    is JavascriptTokenType.Operator.GreaterThan -> {
                        JavascriptValue.Boolean(interpret(statement.lhs).coerceToNumber() > interpret(statement.rhs).coerceToNumber()).toReference()
                    }
                    is JavascriptTokenType.Operator.OrOr -> {
                        val lhsValue = interpret(statement.lhs)
                        if (lhsValue.isTruthy) {
                            lhsValue.toReference()
                        } else {
                            interpret(statement.rhs).toReference()
                        }
                    }
                    is JavascriptTokenType.Operator.AndAnd -> {
                        val lhsValue = interpret(statement.lhs)
                        if (lhsValue.isTruthy) {
                            interpret(statement.rhs).toReference()
                        } else {
                            lhsValue.toReference()
                        }
                    }
                    is JavascriptTokenType.Operator.StrictEquals -> {
                        JavascriptValue.Boolean(interpret(statement.lhs) == interpret(statement.rhs)).toReference()
                    }
                    is JavascriptTokenType.Operator.StrictNotEquals -> {
                        JavascriptValue.Boolean(interpret(statement.lhs) != interpret(statement.rhs)).toReference()
                    }
                    is JavascriptTokenType.Operator.Equals -> {
                        JavascriptValue.Boolean(JavascriptValue.looselyEquals(interpret(statement.lhs), interpret(statement.rhs))).toReference()
                    }
                    is JavascriptTokenType.Operator.NotEquals -> {
                        JavascriptValue.Boolean(!JavascriptValue.looselyEquals(interpret(statement.lhs), interpret(statement.rhs))).toReference()
                    }
                    is JavascriptTokenType.Operator.Assignment -> {
                        val valueToAssign = interpret(statement.rhs)
                        interpretAsReference(statement.lhs).setter?.invoke(valueToAssign)
                            ?: error("Uncaught SyntaxError: Invalid left-hand side in assignment")

                        valueToAssign.toReference()
                    }
                    is JavascriptTokenType.Operator.PlusAssign -> interpretOperatorAssignAsReference(JavascriptTokenType.Operator.Plus, statement)
                    is JavascriptTokenType.Operator.MinusAssign -> interpretOperatorAssignAsReference(JavascriptTokenType.Operator.Minus, statement)
                    is JavascriptTokenType.Operator.MultiplyAssign -> interpretOperatorAssignAsReference(JavascriptTokenType.Operator.Multiply, statement)
                    is JavascriptTokenType.Operator.DivideAssign -> interpretOperatorAssignAsReference(JavascriptTokenType.Operator.Divide, statement)
                    is JavascriptTokenType.Operator.XorAssign -> interpretOperatorAssignAsReference(JavascriptTokenType.Operator.Xor, statement)
                    is JavascriptTokenType.Operator.ModAssign -> interpretOperatorAssignAsReference(JavascriptTokenType.Operator.Mod, statement)
                    is JavascriptTokenType.Comma -> {
                        interpret(statement.lhs)
                        interpretAsReference(statement.rhs)
                    }
                    else -> {
                        error("${statement.operator} is unsupported for binary operations.")
                    }
                }
            }
            is JavascriptExpression.UnaryOperation -> {
                return when (statement.operator) {
                    is JavascriptTokenType.Operator.Not -> {
                        JavascriptValue.Boolean(!interpret(statement.expression).isTruthy).toReference()
                    }
                    is JavascriptTokenType.Operator.Minus -> {
                        interpret(
                            JavascriptExpression.BinaryOperation(
                                operator = JavascriptTokenType.Operator.Multiply,
                                lhs = statement.expression,
                                rhs = JavascriptExpression.Literal(JavascriptValue.Number(-1.0))
                            )
                        ).toReference()
                    }
                    is JavascriptTokenType.Operator.Plus -> {
                        JavascriptValue.Number(interpret(statement.expression).coerceToNumber()).toReference()
                    }
                    is JavascriptTokenType.PlusPlus -> {
                        val reference = interpretAsReference(statement.expression)
                        val newValue = interpret(
                            JavascriptExpression.BinaryOperation(
                                operator = JavascriptTokenType.Operator.Plus,
                                lhs = JavascriptExpression.Literal(reference.value),
                                rhs = JavascriptExpression.Literal(JavascriptValue.Number(1.0))
                            )
                        )
                        reference.setter?.invoke(newValue)
                            ?: error("Uncaught SyntaxError: Invalid left-hand side in assignment")

                        if (statement.isPrefix) {
                            newValue.toReference()
                        } else {
                            reference.value.toReference()
                        }
                    }
                    is JavascriptTokenType.MinusMinus -> {
                        val reference = interpretAsReference(statement.expression)
                        val newValue = interpret(
                            JavascriptExpression.BinaryOperation(
                                operator = JavascriptTokenType.Operator.Minus,
                                lhs = JavascriptExpression.Literal(reference.value),
                                rhs = JavascriptExpression.Literal(JavascriptValue.Number(1.0))
                            )
                        )
                        reference.setter?.invoke(newValue)
                            ?: error("Uncaught SyntaxError: Invalid left-hand side in assignment")

                        if (statement.isPrefix) {
                            newValue.toReference()
                        } else {
                            reference.value.toReference()
                        }
                    }
                    else -> {
                        error("${statement.operator} is unsupported for Uunary operations.")
                    }
                }
            }
            is JavascriptExpression.Reference -> {
                return currentScope.getProperty(statement.name).toReference {
                    currentScope.setProperty(statement.name, it)
                }
            }
            is JavascriptExpression.DotAccess -> {
                val value = when (val value = interpret(statement.expression)) {
                    is JavascriptValue.Object -> value.value
                    else -> error("Cannot access property '${statement.propertyName}' on ${value} since it's not an object.")
                }

                return value.getProperty(statement.propertyName).toReference {
                    value.setProperty(statement.propertyName, it)
                }
            }
            is JavascriptExpression.IndexAccess -> {
                val value = when (val value = interpret(statement.expression)) {
                    is JavascriptValue.Object -> value.value
                    else -> error("Cannot index $value since it's not an object.")
                }

                val property = interpret(statement.indexExpression)
                return value.getProperty(property.toString()).toReference {
                    value.setProperty(property.toString(), it)
                }
            }
            is JavascriptExpression.Literal -> {
                return statement.value.toReference()
            }
            is JavascriptExpression.ObjectLiteral -> {
                return JavascriptValue.Object(
                    JavascriptObject().apply {
                        statement.fields.forEach {
                            setProperty(it.name, interpret(it.rhs))
                        }
                    }
                ).toReference()
            }
            is JavascriptExpression.ArrayLiteral -> {
                return JavascriptValue.Object(
                    JavascriptArray(statement.items.map { interpret(it) })
                ).toReference()
            }
            is JavascriptExpression.AnonymousFunction -> {
                return JavascriptValue.Object(
                    JavascriptFunction(
                       parameterNames = statement.parameterNames,
                        body = statement.body,
                        parentScope = currentScope
                    )
                ).toReference()
            }
            is JavascriptExpression.NewCall -> {
                val constructor = (interpret(statement.function.expression) as JavascriptValue.Object).value

                if (constructor !is JavascriptFunction) {
                    error("TypeError: $constructor is not a constructor")
                }

                val objectThis = JavascriptObject(prototype = constructor.functionPrototype)

                enterFunction(
                    parameterNames = constructor.parameterNames,
                    passedParameters = statement.function.parameters,
                    parentScope = constructor.parentScope,
                    thisBinding = objectThis
                )

                interpret(constructor.body)

                exitFunction()
                return JavascriptValue.Object(objectThis).toReference()
            }
        }
    }

    private fun interpretOperatorAssignAsReference(
        newOperator: JavascriptTokenType.Operator,
        binaryExpression: JavascriptExpression.BinaryOperation
    ): JavascriptReference {
        val reference = interpretAsReference(binaryExpression.lhs)

        val valueToAssign = interpret(
            JavascriptExpression.BinaryOperation(
                operator = newOperator,
                lhs = JavascriptExpression.Literal(reference.value),
                rhs = binaryExpression.rhs
            )
        )

        reference.setter?.invoke(valueToAssign)
            ?: error("Uncaught SyntaxError: Invalid left-hand side in assignment")

        return valueToAssign.toReference()
    }

    private fun enterFunction(
        parameterNames: List<String>,
        passedParameters: List<JavascriptExpression>,
        parentScope: JavascriptScope,
        thisBinding: JavascriptObject? = null
    ) {
        val functionScope = JavascriptScope(
            thisBinding = thisBinding ?: parentScope.thisBinding,
            scopeObject = JavascriptObject().apply {
                    parameterNames.forEachIndexed { index, parameterName ->
                    setProperty(parameterName, interpret(passedParameters.getOrElse(index) {
                        JavascriptExpression.Literal(value = JavascriptValue.Undefined)
                    }))
                }
            },
            parentScope = parentScope
        )

        stack.push(JavascriptStackFrame(functionScope))
    }

    private fun exitFunction() {
        stack.pop()
    }

    private fun interruptControlFlowWith(interruption: ControlFlowInterruption) {
        controlFlowInterruption = interruption
    }

    private inline fun <reified T : ControlFlowInterruption> maybeConsumeControlFlowInterrupt(): T? {
        if (controlFlowInterruption !is T) {
            return null
        }
        return (controlFlowInterruption as T).also { controlFlowInterruption = null }
    }

    private inline fun <reified T : ControlFlowInterruption> consumeControlFlowInterrupt(): T {
        return (controlFlowInterruption as T).also { controlFlowInterruption = null }
    }

    private fun hasControlFlowInterrupted(): Boolean {
        return controlFlowInterruption != null
    }

    private inline fun <reified T : ControlFlowInterruption> hasControlFlowInterruptedDueTo(): Boolean {
        return controlFlowInterruption is T
    }

    sealed class ControlFlowInterruption {
        data class Return(val value: JavascriptValue) : ControlFlowInterruption()
    }
}