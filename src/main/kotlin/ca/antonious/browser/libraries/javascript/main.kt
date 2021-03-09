package ca.antonious.browser.libraries.javascript

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.lexer.JavascriptLexer
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser

fun main() {
    val rawProgram = """
        const pattern = /\d+/g;

    """.trimIndent()

    val tokens = JavascriptLexer(rawProgram).lex()
    val program = JavascriptParser(tokens, rawProgram).parse()
    println(JavascriptInterpreter().interpret(program))
}