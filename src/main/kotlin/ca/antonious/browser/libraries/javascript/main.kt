package ca.antonious.browser.libraries.javascript

import ca.antonious.browser.libraries.console.cyan
import ca.antonious.browser.libraries.console.gray
import ca.antonious.browser.libraries.console.green
import ca.antonious.browser.libraries.console.magenta
import ca.antonious.browser.libraries.console.red
import ca.antonious.browser.libraries.console.yellow
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
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
    
    interpreter.globalObject
        .getProperty("console")
        .requireAsObject()
        .setNonEnumerableNativeFunction("log") {
            println(it.arguments.joinToString(" ") { it.toDescriptiveString() })
            JavascriptValue.Undefined
        }

    var programBuffer = ""
    var indentationLevel = 0
    var programBracketBalance = 0

    while (true) {
        if (programBuffer.isEmpty()) {
            print("> ")
        } else {
            print(".${"..".repeat(indentationLevel)} ")
        }

        val nextLine = readLine() ?: ""
        val lineBracketBalance = nextLine.count { it == '{' } - nextLine.count { it == '}' }
        programBracketBalance += lineBracketBalance

        if (lineBracketBalance > 0) {
            indentationLevel++
        } else if (programBracketBalance < indentationLevel) {
            indentationLevel = programBracketBalance
        }

        programBuffer += "\n${nextLine}"

        if (nextLine == ".exit") {
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

private fun JavascriptValue.toDescriptiveString(indentationLevel: Int = 1, seenObjects: Set<JavascriptObject> = emptySet()): String {
    val previousIndentation = "  ".repeat(indentationLevel - 1)
    val indentation = "  ".repeat(indentationLevel)

    return when (this) {
        is JavascriptValue.Undefined -> "$this".gray()
        is JavascriptValue.Number,
        is JavascriptValue.Boolean -> "$this".yellow()
        is JavascriptValue.String -> "'$this'".green()
        is JavascriptValue.Object -> {
            if (this.requireAsObject() in seenObjects) {
                return "[Circular ref]".cyan()
            }
            when (val obj = this.requireAsObject()) {
                is DateObject -> {
                    obj.date.toString().magenta()
                }
                is ArrayObject -> {
                    val arrayValues = obj.array.map { it.toDescriptiveString() }
                    val arrayProperties = obj.enumerableProperties.map { "${it.first}: ${it.second.toDescriptiveString(indentationLevel + 1)}" }
                    "[ ${(arrayValues + arrayProperties).joinToString(", ")} ]"
                }
                else -> {
                    val objectValues = obj.enumerableProperties.map {
                        "${it.first}: ${it.second.toDescriptiveString(indentationLevel + 1, seenObjects + obj)}"
                    }

                    val charCount = objectValues.sumBy { it.length }
                    val objectStart = if (charCount > 80) {
                        "\n$indentation"
                    } else {
                        " "
                    }

                    val objectEnd= if (charCount > 80) {
                        "\n$previousIndentation"
                    } else {
                        " "
                    }

                    val valueSeparator = if (charCount > 80) {
                        ",\n$indentation"
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
