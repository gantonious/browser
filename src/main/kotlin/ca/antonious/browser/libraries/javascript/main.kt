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

        window.onload = function () {
        	generate()
        	trim()
        	shuffle()
        }

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

        function move(dir) {
        	const sizeInput = document.getElementById("size");
        	const size = parseInt(sizeInput.value)
        	const max = size - 1

        	const oldSquares = document.getElementsByClassName("empty-square")
        	const oldSquare = oldSquares[0]
        	const oldID = oldSquare.id

        	const pattern = /\d+/g;
        	const values = oldID.match(pattern)
        	const oldRow = parseInt(values[0])
        	const oldCol = parseInt(values[1])

        	let newRow = oldRow
        	let newCol = oldCol

        	if (dir === "left") {
        		if (oldCol <= 0) { return }
        		--newCol
        	} else if (dir === "right") {
        		if (oldCol >= max) { return }
        		++newCol
        	} else if (dir === "up") {
        		if (oldRow <= 0) { return }
        		--newRow
        	} else if (dir === "down") {
        		if (oldRow >= max) { return }
        		++newRow
        	}

        	const newID = "row" + newRow + "col" + newCol
        	const newSquare = document.getElementById(newID)

        	const oldSpans = oldSquare.children
        	const oldSpan = oldSpans[1]

        	const newSpans = newSquare.children
        	const newSpan = newSpans[1]

        	const tmp = oldSpan.innerHTML
        	oldSpan.innerHTML = newSpan.innerHTML
        	newSpan.innerHTML = tmp

        	oldSquare.classList.remove("empty-square")
        	newSquare.classList.add("empty-square")

        	oldSquare.classList.remove("good-square")
        	oldSquare.classList.remove("bad-square")
        	newSquare.classList.remove("good-square")
        	newSquare.classList.remove("bad-square")

        	if (oldCol + oldRow * (max + 1) + 1 == oldSpan.innerHTML) {
        		oldSquare.classList.add("good-square")
        	} else {
        		oldSquare.classList.add("bad-square")
        	}
        }

    """.trimIndent()

    val tokens = JavascriptLexer(rawProgram).lex()
    val program = JavascriptParser(tokens, rawProgram).parse()
    println(JavascriptInterpreter().interpret(program))
}