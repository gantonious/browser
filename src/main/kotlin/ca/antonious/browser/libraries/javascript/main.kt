package ca.antonious.browser.libraries.javascript

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.lexer.JavascriptLexer
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser

fun main() {
    val rawProgram = """
        function shuffle() {
        	let i;
        	for (i = 0; i < 1000; i++) {
        		const num = Math.floor(Math.random() * 4) + 1;
        		if (num === 1) {
        			move("up")
        		} else if (num === 2) {
        			move("left")
        		} else if (num === 3) {
        			move("down")
        		} else if (num === 4) {
        			move("right")
        		}
        	}
        }
    """.trimIndent()

    val tokens = JavascriptLexer(rawProgram).lex()
    val program = JavascriptParser(tokens, rawProgram).parse()
    println(JavascriptInterpreter().interpret(program))
}