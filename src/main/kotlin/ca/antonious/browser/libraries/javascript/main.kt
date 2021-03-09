package ca.antonious.browser.libraries.javascript

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.lexer.JavascriptLexer
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser

fun main() {
    val rawProgram = """
        window.onkeydown = function (event) {
            if (event.code === "ArrowUp") {
                move("up")
            } else if (event.code === "ArrowLeft") {
                move("left")
            } else if (event.code === "ArrowDown") {
                move("down")
            } else if (event.code === "ArrowRight") {
                move("right")
            }
        }

    """.trimIndent()

    val tokens = JavascriptLexer(rawProgram).lex()
    val program = JavascriptParser(tokens, rawProgram).parse()
    println(JavascriptInterpreter().interpret(program))
}