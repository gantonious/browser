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
                    value = JavascriptFunction.UserDefined(
                        parameterNames = statement.parameterNames,
                        body = statement.body,
                        parentScope = currentScope
                    )
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
                        enterFunction(function, statement.parameters)

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
                return when (statement.operator) {
                    is JavascriptTokenType.Operator.Plus -> {
                        val lhsValue = interpret(statement.lhs)
                        val rhsValue = interpret(statement.rhs)

                        if (lhsValue is JavascriptValue.String|| rhsValue is JavascriptValue.String) {
                            JavascriptValue.String(lhsValue.toString() + rhsValue.toString())
                        } else {
                            JavascriptValue.Number(lhsValue.coerceToNumber() + rhsValue.coerceToNumber())
                        }
                    }
                    is JavascriptTokenType.Operator.Minus -> {
                        JavascriptValue.Number(interpret(statement.lhs).coerceToNumber() - interpret(statement.rhs).coerceToNumber())
                    }
                    is JavascriptTokenType.Operator.Multiply -> {
                        JavascriptValue.Number(interpret(statement.lhs).coerceToNumber() * interpret(statement.rhs).coerceToNumber())
                    }
                    is JavascriptTokenType.Operator.Xor -> {
                        val result = (
                            interpret(statement.lhs).coerceToNumber().toInt() xor
                            interpret(statement.rhs).coerceToNumber().toInt()
                        ).toDouble()

                        JavascriptValue.Number(result)
                    }
                    is JavascriptTokenType.Operator.Mod -> {
                        JavascriptValue.Number(interpret(statement.lhs).coerceToNumber() % interpret(statement.rhs).coerceToNumber())
                    }
                    is JavascriptTokenType.Operator.LessThanOrEqual -> {
                        JavascriptValue.Boolean(interpret(statement.lhs).coerceToNumber() <= interpret(statement.rhs).coerceToNumber())
                    }
                    is JavascriptTokenType.Operator.LessThan -> {
                        JavascriptValue.Boolean(interpret(statement.lhs).coerceToNumber() < interpret(statement.rhs).coerceToNumber())
                    }
                    is JavascriptTokenType.Operator.GreaterThanOrEqual -> {
                        JavascriptValue.Boolean(interpret(statement.lhs).coerceToNumber() >= interpret(statement.rhs).coerceToNumber())
                    }
                    is JavascriptTokenType.Operator.GreaterThan -> {
                        JavascriptValue.Boolean(interpret(statement.lhs).coerceToNumber() > interpret(statement.rhs).coerceToNumber())
                    }
                    is JavascriptTokenType.Operator.OrOr -> {
                        val lhsValue = interpret(statement.lhs)
                        if (lhsValue.isTruthy) {
                            lhsValue
                        } else {
                            interpret(statement.rhs)
                        }
                    }
                    is JavascriptTokenType.Operator.AndAnd -> {
                        val lhsValue = interpret(statement.lhs)
                        if (lhsValue.isTruthy) {
                            interpret(statement.rhs)
                        } else {
                            lhsValue
                        }
                    }
                    is JavascriptTokenType.Operator.StrictEquals -> {
                        JavascriptValue.Boolean(interpret(statement.lhs) == interpret(statement.rhs))
                    }
                    is JavascriptTokenType.Operator.Equals -> {
                        JavascriptValue.Boolean(JavascriptValue.looselyEquals(interpret(statement.lhs), interpret(statement.rhs)))
                    }
                    else -> {
                        error("${statement.operator} is unsupported for binary operations.")
                    }
                }
            }
            is JavascriptExpression.UnaryOperation -> {
                return when (statement.operator) {
                    is JavascriptTokenType.Operator.Not -> {
                        JavascriptValue.Boolean(!interpret(statement.expression).isTruthy)
                    }
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
                return JavascriptValue.Function(
                    JavascriptFunction.UserDefined(
                        parameterNames =  statement.parameterNames,
                        body = statement.body,
                        parentScope = currentScope
                    )
                )
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