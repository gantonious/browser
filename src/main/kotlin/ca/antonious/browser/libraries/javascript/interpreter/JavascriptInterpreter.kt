package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.*
import ca.antonious.browser.libraries.javascript.interpreter.builtins.JavascriptFunction
import ca.antonious.browser.libraries.javascript.lexer.JavascriptLexer
import ca.antonious.browser.libraries.javascript.lexer.JavascriptTokenType
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser
import java.util.*
import kotlin.random.Random

class JavascriptInterpreter {
    val globalObject = JavascriptObject().apply {
        setNativeFunction("getInput") {
            val inputText = it.firstOrNull() as? JavascriptValue.String
            if (inputText != null) {
                print(inputText.value)
            }
            JavascriptValue.Number((readLine() ?: "").toDouble())
        }

        setNativeFunction("parseInt") {
            JavascriptValue.Number(it.first().coerceToNumber().toInt().toDouble())
        }

        setProperty("console", JavascriptValue.Object(JavascriptObject().apply {
            setNativeFunction("log") {
                println(it.joinToString(separator = " "))
                JavascriptValue.Undefined
            }
        }))

        setProperty("Math", JavascriptValue.Object(JavascriptObject().apply {
            setNativeFunction("floor") {
                JavascriptValue.Number(it.first().coerceToNumber().toInt().toDouble())
            }

            setNativeFunction("random") {
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
            is JavascriptStatement.Block -> {
                var result: JavascriptReference = JavascriptReference.Undefined
                for (child in statement.body) {
                    result = interpretAsReference(child)
                }

                return result
            }
            is JavascriptStatement.Function -> {
                val value = JavascriptValue.Function(
                    value = JavascriptFunction.UserDefined(
                        parameterNames = statement.parameterNames,
                        body = statement.body,
                        parentScope = currentScope
                    )
                )
                currentScope.setProperty(key = statement.name, value = value)
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.Return -> {
                if (statement.expression != null) {
                    lastReturn = interpretAsReference(statement.expression)
                }

                return JavascriptReference.Undefined
            }
            is JavascriptExpression.FunctionCall -> {
                val callableValue = interpret(statement.expression)

                if (callableValue !is JavascriptValue.Function) {
                    error("Can't call non-function type '$callableValue'.")
                }

                when (val function = callableValue.value) {
                    is JavascriptFunction.Native -> {
                        return function.body.invoke(statement.parameters.map { interpret(it) })
                    }
                    is JavascriptFunction.UserDefined -> {
                        enterFunction(function, statement.parameters)

                        for (child in function.body.body) {
                            interpret(child)
                            if (lastReturn != null) {
                                break
                            }
                        }

                        exitFunction()
                        return (lastReturn ?: JavascriptReference.Undefined).also {
                            lastReturn = null
                        }
                    }
                }
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
            is JavascriptStatement.WhileLoop -> {
                while (interpret(statement.condition).isTruthy) {
                    interpret(statement.body)
                }
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.ForLoop -> {
                interpret(statement.initializerExpression)

                while (interpret(statement.conditionExpression).isTruthy) {
                    interpret(statement.body)
                    interpret(statement.updaterExpression)
                }

                return JavascriptReference.Undefined
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
                    is JavascriptTokenType.Operator.Equals -> {
                        JavascriptValue.Boolean(JavascriptValue.looselyEquals(interpret(statement.lhs), interpret(statement.rhs))).toReference()
                    }
                    is JavascriptTokenType.Operator.Assignment -> {
                        val valueToAssign = interpret(statement.rhs)
                        interpretAsReference(statement.lhs).setter?.invoke(valueToAssign)
                            ?: error("Uncaught SyntaxError: Invalid left-hand side in assignment")

                        valueToAssign.toReference()
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
            is JavascriptExpression.AnonymousFunction -> {
                return JavascriptValue.Function(
                    JavascriptFunction.UserDefined(
                        parameterNames = statement.parameterNames,
                        body = statement.body,
                        parentScope = currentScope
                    )
                ).toReference()
            }
        }
    }

    private fun enterFunction(function: JavascriptFunction.UserDefined, passedParameters: List<JavascriptExpression>) {
        val functionScope = JavascriptScope(
            thisBinding = globalObject,
            scopeObject = JavascriptObject().apply {
                    function.parameterNames.forEachIndexed { index, parameterName ->
                    setProperty(parameterName, interpret(passedParameters.getOrElse(index) {
                        JavascriptExpression.Literal(value = JavascriptValue.Undefined)
                    }))
                }
            },
            parentScope = function.parentScope
        )

        stack.push(JavascriptStackFrame(functionScope))
    }

    private fun exitFunction() {
        stack.pop()
    }
}