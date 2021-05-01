package ca.antonious.browser.libraries.javascript

import ca.antonious.browser.libraries.console.gray
import ca.antonious.browser.libraries.console.green
import ca.antonious.browser.libraries.console.yellow
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.`object`.ObjectPrototype
import ca.antonious.browser.libraries.javascript.interpreter.builtins.array.ArrayObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.number.NumberObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.string.StringObject
import ca.antonious.browser.libraries.javascript.interpreter.testrunner.asA
import ca.antonious.browser.libraries.javascript.lexer.JavascriptLexer
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser

fun main() {
    val interpreter = JavascriptInterpreter()

    var programBuffer = ""

    while (true) {
        if (programBuffer.isEmpty()) {
            print("> ")
        } else {
            print("... ")
        }

        programBuffer += readLine() ?: ""

        if (programBuffer == ".exit") {
            break
        }


        try {
            val tokens = JavascriptLexer(programBuffer, sourceFilename = "REPL").lex()
            val program = try {
                JavascriptParser(tokens, programBuffer).parse()
            } catch (ex: Exception) {
                continue
            }

            println(interpreter.interpret(program).toReplOutputString())
        } catch (e: Exception) {
            println(e.message)
        }

        programBuffer = ""
    }
}

private fun JavascriptValue.toReplOutputString(): String {
    return when (this) {
        is JavascriptValue.Undefined -> "$this".gray()
        is JavascriptValue.Number,
        is JavascriptValue.Boolean -> "$this".yellow()
        is JavascriptValue.String -> "'$this'".green()
        is JavascriptValue.Object -> {
            when (val obj = this.requireAsObject()) {
                is ArrayObject -> {
                    val arrayValues = obj.array.map { it.toReplOutputString() }
                    val arrayProperties = obj.enumerableProperties.map { "${it.first}: ${it.second.toReplOutputString()}" }
                    "[ ${(arrayValues + arrayProperties).joinToString(", ")} ]"
                }
                else -> {
                    val objectValues = obj.enumerableProperties.map { "${it.first}: ${it.second.toReplOutputString()}" }
                    val objectBody = "{ ${objectValues.joinToString(", ")} }".trimStart()

                    when (obj) {
                        is StringObject -> "[String: ${obj.value}]".green() + if (objectValues.isEmpty()) "" else " $objectBody"
                        is NumberObject -> "[Number: ${obj.value}]".yellow() + if (objectValues.isEmpty()) "" else " $objectBody"
                        else -> objectBody
                    }
                }
            }

        }
        else -> this.toString()
    }
}
