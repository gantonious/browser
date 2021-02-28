package ca.antonious.browser.libraries.javascript

import ca.antonious.browser.libraries.javascript.ast.BooleanOperator
import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptNode
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.parser.ExpressionParser
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser

fun main() {
    val rawProgram = """
        function test2(var) {
            if (var) {
                return 1
            }
            return 2
        }
        
        function test() {
            return 5+2*test2(2)          
        }
        
        test()
    """.trimIndent()

    val program = JavascriptNode.Program(body = JavascriptParser().parse(rawProgram))
    println(JavascriptInterpreter().interpret(program))
}