package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.BooleanOperator
import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptNode
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

class JavascriptInterpreter {
    private val globalObject = JavascriptObject().apply {
        setProperty("consoleLog", NativeFunction {
            println("${it.first()}")
            JavascriptValue.Undefined
        })

        setProperty("input", NativeFunction {
            JavascriptValue.Double((readLine() ?: "").toDouble())
        })

        setProperty("console", JavascriptValue.Object(JavascriptObject().apply {
                setProperty("log", NativeFunction {
                    println("${it.first()}")
                    JavascriptValue.Undefined
                })
            })
        )
    }

    private var currentScope = globalObject
    private var lastReturn: JavascriptValue? = null

    fun interpret(node: JavascriptNode): JavascriptValue {
        when (node) {
            is JavascriptNode.Program -> {
                return interpretChildren(node.body)
            }
            is JavascriptNode.Function -> {
                currentScope.setProperty(key = node.name, value = node)
                return JavascriptValue.Undefined
            }
            is JavascriptNode.Return -> {
                lastReturn = interpret(node.expression)
                return JavascriptValue.Undefined
            }
            is JavascriptExpression.FunctionCall -> {
                val function = currentScope.getProperty(node.name)

                if (function is NativeFunction) {
                    return function.body.invoke(node.parameters.map { interpret(it) })
                } else if (function !is JavascriptNode.Function) {
                    error("Cannot invoke function on undefined '${node.name}'")
                }

                enterFunction(function, node.parameters)

                for (child in function.body) {
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
            is JavascriptNode.IfStatement -> {
                val ifConditionValue = interpret(node.expression)
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

                if (lhsValue !is JavascriptValue.Double) {
                    error("Can't execute math on '${lhsValue}'")
                }

                if (rhsValue !is JavascriptValue.Double) {
                    error("Can't execute math on '${rhsValue}'")
                }

                return when (node.operator) {
                    is BooleanOperator.Add -> {
                        JavascriptValue.Double(lhsValue.value + rhsValue.value)
                    }
                    is BooleanOperator.Subtract -> {
                        JavascriptValue.Double(lhsValue.value - rhsValue.value)
                    }
                    is BooleanOperator.Multiply -> {
                        JavascriptValue.Double(lhsValue.value * rhsValue.value)
                    }
                    is BooleanOperator.LessThan -> {
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