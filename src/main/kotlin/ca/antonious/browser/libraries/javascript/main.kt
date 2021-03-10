package ca.antonious.browser.libraries.javascript

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.lexer.JavascriptLexer
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser
import java.lang.Exception

fun main() {
    val interpreter = JavascriptInterpreter()
    while (true) {
        print("> ")
        val lineToExecute = readLine() ?: ""

        if (lineToExecute == ".exit") {
            break
        }

        try {
            val tokens = JavascriptLexer(lineToExecute).lex()
            val program = JavascriptParser(tokens, lineToExecute).parse()
            println(
                when (val value = interpreter.interpret(program)) {
                    is JavascriptValue.String -> "'$value'"
                    else -> value.toString()
                }
            )
        } catch (e: Exception) {
            println(e.message)
        }
    }
}