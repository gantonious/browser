package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.BooleanOperator
import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptNode
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

class JavascriptInterpreter {
    private val globalObject = JavascriptObject()
    private var currentScope = globalObject

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
                return interpret(node.expression)
            }
            is JavascriptExpression.FunctionCall -> {
                val function = currentScope.getProperty(node.name) as? JavascriptNode.Function ?: error("Cannot invoke function on undefined '${node.name}'")
                enterFunction(function, node.parameters)
                val result = interpretChildren(function.body)
                exitFunction()
                return result
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
                }
            }
            is JavascriptExpression.Reference -> {
                return interpret(currentScope.getProperty(node.name) as? JavascriptNode ?: JavascriptExpression.Literal(value = JavascriptValue.Undefined))
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
                setProperty(parameterName, passedParameters[index])
            }
        }
    }

    private fun exitFunction() {
        currentScope = currentScope.parent ?: globalObject
    }
}