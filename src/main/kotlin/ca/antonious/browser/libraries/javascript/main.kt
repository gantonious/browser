package ca.antonious.browser.libraries.javascript

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.lexer.JavascriptLexer
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser

fun main() {
    val rawProgram = """
        function generate() {
            const sizeInput = document.getElementById("size")
            const size = parseInt(sizeInput.value)
        
            const boardDiv = document.getElementById("board")
            boardDiv.innerHTML = ''
        
            let number = 1
            let row
            let col
            for (row = 0; row < size; row++) {
                let rowDiv = document.createElement("div")
                boardDiv.appendChild(rowDiv)
                for (col = 0; col < size; col++) {
                    let colDiv = document.createElement("div")
                    colDiv.id = "row" + row + "col" + col
                    colDiv.className = "square good-square"
                    colDiv.innerHTML = "<img><span>" + number++ + "</span>"
                    rowDiv.appendChild(colDiv)
                }
            }
        }
    """.trimIndent()

    val tokens = JavascriptLexer(rawProgram).lex()
    val program = JavascriptParser(tokens, rawProgram).parse()
    println(JavascriptInterpreter().interpret(program))
}