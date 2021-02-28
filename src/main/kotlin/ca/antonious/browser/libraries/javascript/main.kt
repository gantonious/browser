package ca.antonious.browser.libraries.javascript

import ca.antonious.browser.libraries.javascript.ast.BooleanOperator
import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptNode
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.parser.ExpressionParser
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser

fun main() {
    val program2 = JavascriptNode.Program(
        body = listOf(
            JavascriptNode.Function(
                name = "test",
                body = listOf(
                    JavascriptNode.Return(
                        expression = JavascriptExpression.BooleanOperation(
                            operator = BooleanOperator.Add,
                            lhs = JavascriptExpression.Literal(value = JavascriptValue.Double(4.0)),
                            rhs = JavascriptExpression.Literal(value = JavascriptValue.Double(5.0))
                        )
                    )
                )
            ),
            JavascriptExpression.FunctionCall(name = "test", parameters = emptyList())
        )
    )

    val rawProgram = """
        function test2() {
            return 10 * 5 - 3
        }
        
        function test() {
            return 5 + 2 * test2()          
        }
        
        test()
    """.trimIndent()

    val program = JavascriptNode.Program(body = JavascriptParser().parse(rawProgram))
    println(JavascriptInterpreter().interpret(program))

//    val expression = "5 + func(1, anotherFunc(2)) * 3"

//    println(program)
}