package ca.antonious.browser.libraries.javascript

import ca.antonious.browser.libraries.console.cyan
import ca.antonious.browser.libraries.console.gray
import ca.antonious.browser.libraries.console.green
import ca.antonious.browser.libraries.console.magenta
import ca.antonious.browser.libraries.console.red
import ca.antonious.browser.libraries.console.yellow
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.builtins.array.ArrayObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.date.DateObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.ClassConstructor
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.FunctionObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.number.NumberObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.string.StringObject
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
                if (programBuffer.endsWith(";")) {
                    println(ex.message?.red())
                    programBuffer = ""
                }
                continue
            }

            println(interpreter.interpret(program).toDescriptiveString())
        } catch (ex: Exception) {
            println(ex.message?.red())
        }

        programBuffer = ""
    }
}

private fun JavascriptValue.toDescriptiveString(): String {
    return when (this) {
        is JavascriptValue.Undefined -> "$this".gray()
        is JavascriptValue.Number,
        is JavascriptValue.Boolean -> "$this".yellow()
        is JavascriptValue.String -> "'$this'".green()
        is JavascriptValue.Object -> {
            when (val obj = this.requireAsObject()) {
                is DateObject -> {
                    obj.date.toString().magenta()
                }
                is ArrayObject -> {
                    val arrayValues = obj.array.map { it.toDescriptiveString() }
                    val arrayProperties = obj.enumerableProperties.map { "${it.first}: ${it.second.toDescriptiveString()}" }
                    "[ ${(arrayValues + arrayProperties).joinToString(", ")} ]"
                }
                else -> {
                    val objectValues = obj.enumerableProperties.map { "${it.first}: ${it.second.toDescriptiveString()}" }
                    val charCount = objectValues.sumBy { it.length }
                    val objectStart = if (charCount > 80) {
                        "\n  "
                    } else {
                        " "
                    }

                    val objectEnd= if (charCount > 80) {
                        "\n"
                    } else {
                        " "
                    }

                    val valueSeparator = if (charCount > 80) {
                        ",\n  "
                    } else {
                        ", "
                    }
                    val objectBody = "{$objectStart${objectValues.joinToString(valueSeparator)}$objectEnd}".trimStart()

                    when (obj) {
                        is StringObject -> "[String: ${obj.value}]".green() + if (objectValues.isEmpty()) "" else " $objectBody"
                        is NumberObject -> "[Number: ${obj.value}]".yellow() + if (objectValues.isEmpty()) "" else " $objectBody"
                        is ClassConstructor -> "[class: ${obj.name}]".cyan() + if (objectValues.isEmpty()) "" else " $objectBody"
                        is FunctionObject -> "[Function: ${obj.name}]".cyan() + if (objectValues.isEmpty()) "" else " $objectBody"
                        else -> objectBody
                    }
                }
            }

        }
        else -> this.toString()
    }
}
