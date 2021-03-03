package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptBooleanOperator
import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptNode
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

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

    private var currentScope = globalObject
    private var lastReturn: JavascriptValue? = null

    fun interpret(node: JavascriptNode): JavascriptValue {
        when (node) {
            is JavascriptNode.Program -> {
                return interpretChildren(node.body)
            }
            is JavascriptNode.Function -> {
                val value = JavascriptValue.Function(
                    value = JavascriptFunction.UserDefined(functionNode = node)
                )
                currentScope.setProperty(key = node.name, value = value)
                return JavascriptValue.Undefined
            }
            is JavascriptNode.Return -> {
                lastReturn = interpret(node.expression)
                return JavascriptValue.Undefined
            }
            is JavascriptExpression.FunctionCall -> {
                val callableValue = interpret(node.expression)

                if (callableValue !is JavascriptValue.Function) {
                    error("Can't call non-function type '$callableValue'.")
                }

                when (val function = callableValue.value) {
                    is JavascriptFunction.Native -> {
                        return function.body.invoke(node.parameters.map { interpret(it) })
                    }
                    is JavascriptFunction.UserDefined -> {
                        enterFunction(function.functionNode, node.parameters)

                        for (child in function.functionNode.body) {
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
            is JavascriptNode.IfStatement -> {
                val ifConditionValue = interpret(node.condition)
                if (ifConditionValue.isTruthy) {
                    interpretChildren(node.body)
                }
                return JavascriptValue.Undefined
            }
            is JavascriptNode.LetAssignment -> {
                currentScope.setProperty(node.name, interpret(node.expression))
                return JavascriptValue.Undefined
            }
            is JavascriptNode.WhileLoop -> {
                while (interpret(node.condition).isTruthy) {
                    interpretChildren(node.body)
                }
                return JavascriptValue.Undefined
            }
            is JavascriptExpression.BooleanOperation -> {
                val lhsValue = interpret(node.lhs)
                val rhsValue = interpret(node.rhs)

                if (lhsValue !is JavascriptValue.Number) {
                    error("Expected left hand side of operand '${lhsValue}' to be a number.")
                }

                if (rhsValue !is JavascriptValue.Number) {
                    error("Expected right hand side of operand '${rhsValue}' to be a number.")
                }

                return when (node.operator) {
                    is JavascriptBooleanOperator.Add -> {
                        JavascriptValue.Number(lhsValue.value + rhsValue.value)
                    }
                    is JavascriptBooleanOperator.Subtract -> {
                        JavascriptValue.Number(lhsValue.value - rhsValue.value)
                    }
                    is JavascriptBooleanOperator.Multiply -> {
                        JavascriptValue.Number(lhsValue.value * rhsValue.value)
                    }
                    is JavascriptBooleanOperator.LessThan -> {
                        JavascriptValue.Boolean(lhsValue.value < rhsValue.value)
                    }
                }
            }
            is JavascriptExpression.Reference -> {
                return when (val value = currentScope.getProperty(node.name)) {
                    is JavascriptValue -> value
                    is JavascriptNode -> interpret(value)
                    else -> JavascriptValue.Undefined
                }
            }
            is JavascriptExpression.DotAccess -> {
                val value = when (val value = interpret(node.expression)) {
                    is JavascriptValue.Object -> value.value
                    else -> error("Cannot access property '${node.propertyName}' on ${value} since it's not an object.")
                }

                return when (val property = value.getProperty(node.propertyName)) {
                    is JavascriptValue -> property
                    is JavascriptNode -> interpret(property)
                    else -> JavascriptValue.Undefined
                }
            }
            is JavascriptExpression.Literal -> {
                return node.value
            }
        }
    }

    private fun interpretChildren(nodes: List<JavascriptNode>): JavascriptValue {
        var result: JavascriptValue = JavascriptValue.Undefined
        for (child in nodes) {
            result = interpret(child)
        }

        return result
    }

    private fun enterFunction(function: JavascriptNode.Function, passedParameters: List<JavascriptExpression>) {
        currentScope = JavascriptObject(currentScope).apply {
            function.parameterNames.forEachIndexed { index, parameterName ->
                setProperty(parameterName, interpret(passedParameters.getOrElse(index) {
                    JavascriptExpression.Literal(value = JavascriptValue.Undefined)
                }))
            }
        }
    }

    private fun exitFunction() {
        currentScope = currentScope.parent ?: globalObject
    }
}