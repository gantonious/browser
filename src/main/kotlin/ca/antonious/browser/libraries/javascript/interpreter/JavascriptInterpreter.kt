package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.BooleanOperator
import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptNode
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

class JavascriptInterpreter {
    private val globalObject = JavascriptObject()

    fun interpret(node: JavascriptNode): JavascriptValue {
        when (node) {
            is JavascriptNode.Program -> {
                return interpretChildren(node.body)
            }
            is JavascriptNode.Function -> {
                globalObject.properties[node.name] = node
                return JavascriptValue.Undefined
            }
            is JavascriptNode.Return -> {
                return interpret(node.expression)
            }
            is JavascriptExpression.FunctionCall -> {
                val function = globalObject.properties[node.name] as? JavascriptNode.Function ?: error("Cannot invoke function on undefined '${node.name}'")
                return interpretChildren(function.body)
            }
            is JavascriptExpression.BooleanOperation -> {
                val lhsValue = interpret(node.lhs) as? JavascriptValue.Double ?: error("")
                val rhsValue = interpret(node.rhs) as? JavascriptValue.Double ?: error("")

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
                return globalObject.properties[node.name] as JavascriptValue
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
}