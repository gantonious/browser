package ca.antonious.browser.libraries.javascript

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.lexer.JavascriptLexer
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser

fun main() {
    val rawProgram = """
        function trim() {
        	const sizeInput = document.getElementById("size");
        	const size = parseInt(sizeInput.value)
        	const max = size - 1

        	const maxID = "row" + max + "col" + max
        	const maxSquare = document.getElementById(maxID)
        	maxSquare.classList.remove("good-square")
        	maxSquare.classList.remove("bad-square")
        	maxSquare.classList.add("empty-square")
        	const maxSpans = maxSquare.children
        	const maxSpan = maxSpans[1]
        	maxSpan.innerHTML = ""
        }
    """.trimIndent()

    val tokens = JavascriptLexer(rawProgram).lex()
    val program = JavascriptParser(tokens, rawProgram).parse()
    println(JavascriptInterpreter().interpret(program))
}