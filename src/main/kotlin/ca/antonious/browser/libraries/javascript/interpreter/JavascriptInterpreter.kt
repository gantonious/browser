package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.*
import ca.antonious.browser.libraries.javascript.interpreter.builtins.JavascriptFunction
import ca.antonious.browser.libraries.javascript.lexer.JavascriptLexer
import ca.antonious.browser.libraries.javascript.lexer.JavascriptTokenType
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser
import java.util.*

class JavascriptInterpreter {
    val globalObject = JavascriptObject().apply {
        setNativeFunction("getInput") {
            val inputText = it.firstOrNull() as? JavascriptValue.String
            if (inputText != null) {
                print(inputText.value)
            }
            JavascriptValue.Number((readLine() ?: "").toDouble())
        }

        setProperty("console", JavascriptValue.Object(JavascriptObject().apply {
            setNativeFunction("log") {
                println(it.joinToString(separator = " "))
                JavascriptValue.Undefined
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

    private var lastReturn: JavascriptValue? = null

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
        when (statement) {
            is JavascriptStatement.Block -> {
                var result: JavascriptValue = JavascriptValue.Undefined
                for (child in statement.body) {
                    result = interpret(child)
                }

                return result
            }
            is JavascriptStatement.Function -> {
                val value = JavascriptValue.Function(
                    value = JavascriptFunction.UserDefined(statement.parameterNames, statement.body)
                )
                currentScope.setProperty(key = statement.name, value = value)
                return JavascriptValue.Undefined
            }
            is JavascriptStatement.Return -> {
                if (statement.expression != null) {
                    lastReturn = interpret(statement.expression)
                }

                return JavascriptValue.Undefined
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
                        enterFunction(function.parameterNames, statement.parameters)

                        for (child in function.body.body) {
                            interpret(child)
                            if (lastReturn != null) {
                                break
                            }
                        }

                        exitFunction()
                        return (lastReturn ?: JavascriptValue.Undefined).also {
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
                return JavascriptValue.Undefined
            }
            is JavascriptStatement.LetAssignment -> {
                currentScope.setProperty(statement.name, interpret(statement.expression))
                return JavascriptValue.Undefined
            }
            is JavascriptStatement.ConstAssignment -> {
                currentScope.setProperty(statement.name, interpret(statement.expression))
                return JavascriptValue.Undefined
            }
            is JavascriptStatement.WhileLoop -> {
                while (interpret(statement.condition).isTruthy) {
                    interpret(statement.body)
                }
                return JavascriptValue.Undefined
            }
            is JavascriptStatement.ForLoop -> {
                interpret(statement.initializerExpression)

                while (interpret(statement.conditionExpression).isTruthy) {
                    interpret(statement.body)
                    interpret(statement.updaterExpression)
                }

                return JavascriptValue.Undefined
            }
            is JavascriptExpression.BinaryOperation -> {
                val lhsValue = interpret(statement.lhs)
                val rhsValue = interpret(statement.rhs)

                if (lhsValue !is JavascriptValue.Number) {
                    error("Expected left hand side of operand '${lhsValue}' to be a number.")
                }

                if (rhsValue !is JavascriptValue.Number) {
                    error("Expected right hand side of operand '${rhsValue}' to be a number.")
                }

                return when (statement.operator) {
                    is JavascriptTokenType.Operator.Plus -> {
                        JavascriptValue.Number(lhsValue.value + rhsValue.value)
                    }
                    is JavascriptTokenType.Operator.Minus -> {
                        JavascriptValue.Number(lhsValue.value - rhsValue.value)
                    }
                    is JavascriptTokenType.Operator.Multiply -> {
                        JavascriptValue.Number(lhsValue.value * rhsValue.value)
                    }
                    is JavascriptTokenType.Operator.LessThan -> {
                        JavascriptValue.Boolean(lhsValue.value < rhsValue.value)
                    }
                    else -> {
                        error("${statement.operator} is unsupported for binary operations.")
                    }
                }
            }
            is JavascriptExpression.UnaryOperation -> {
                return when (statement.operator) {
                    else -> {
                        error("${statement.operator} is unsupported for Uunary operations.")
                    }
                }
            }
            is JavascriptExpression.Reference -> {
                return currentScope.getProperty(statement.name)
            }
            is JavascriptExpression.DotAccess -> {
                val value = when (val value = interpret(statement.expression)) {
                    is JavascriptValue.Object -> value.value
                    else -> error("Cannot access property '${statement.propertyName}' on ${value} since it's not an object.")
                }

                return value.getProperty(statement.propertyName)
            }
            is JavascriptExpression.IndexAccess -> {
                val value = when (val value = interpret(statement.expression)) {
                    is JavascriptValue.Object -> value.value
                    else -> error("Cannot index $value since it's not an object.")
                }

                val property = interpret(statement.indexExpression)
                return value.getProperty(property.toString())
            }
            is JavascriptExpression.Literal -> {
                return statement.value
            }
            is JavascriptExpression.AnonymousFunction -> {
                return JavascriptValue.Function(JavascriptFunction.UserDefined(statement.parameterNames, statement.body))
            }
        }
    }

    private fun enterFunction(parameterNames: List<String>, passedParameters: List<JavascriptExpression>) {
        val functionScope = JavascriptScope(
            thisBinding = globalObject,
            scopeObject = JavascriptObject().apply {
                    parameterNames.forEachIndexed { index, parameterName ->
                    setProperty(parameterName, interpret(passedParameters.getOrElse(index) {
                        JavascriptExpression.Literal(value = JavascriptValue.Undefined)
                    }))
                }
            },
            parentScope = stack.peek().scope
        )

        stack.push(JavascriptStackFrame(functionScope))
    }

    private fun exitFunction() {
        stack.pop()
    }
}