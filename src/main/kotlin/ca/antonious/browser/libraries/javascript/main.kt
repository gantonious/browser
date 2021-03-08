package ca.antonious.browser.libraries.javascript

import ca.antonious.browser.libraries.javascript.ast.JavascriptStatement
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser

fun main() {
    val rawProgram = """
        function test2(var) {
            if (var < 5) {
                return 1
            }
            return 2
        }
        
        function test(number) {
            return 5*test2(number)          
        }
        
        function whileTest(max) {
            let counter = 0
            while (counter < max) {
                console.log(counter)
                let counter = counter + 1 
            }
            
            return test2(counter)
        }

        console.log("test1", "test2")
        whileTest(getInput("Type number: "))
    """.trimIndent()

    println(JavascriptInterpreter().interpret(rawProgram))
}