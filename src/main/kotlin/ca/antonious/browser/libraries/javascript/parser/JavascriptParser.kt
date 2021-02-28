package ca.antonious.browser.libraries.javascript.parser

import ca.antonious.browser.libraries.javascript.ast.BooleanOperator
import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptNode
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import kotlin.math.exp

class JavascriptParser {
    fun parse(rawJavascript: String): List<JavascriptNode> {
        val scanner = StringScanner(rawJavascript)
        val body = mutableListOf<JavascriptNode>()

        while (!scanner.isAtEnd) {
            scanner.moveAfterWhitespace()
            val nextWord = scanner.scanUntil { it.isWhitespace() }

            if (nextWord == "function") {
                val name = scanner.scanUntil(char ='(')
                val params = scanner.scanUntil(char = ')')
                scanner.scanUntil('{')
                val block = scanner.scanUntil('}')
                body += JavascriptNode.Function(name = name, body = parse(block))
            } else if (nextWord == "return") {
                val rawReturnExpression = scanner.scanUntil { it == ';' || it == '\n' }
                body += JavascriptNode.Return(expression = ExpressionParser(rawReturnExpression).parse())
            } else if (nextWord.isNotEmpty()) {
                body += ExpressionParser(nextWord).parse()
            }
        }

        return body
    }
}

class ExpressionParser(expression: String) {
    private val scanner = StringScanner(expression)

    fun parse(): JavascriptExpression {
       return parseAdditionExpression()
    }

    private fun parseAdditionExpression(): JavascriptExpression {
        val lhs = parseMultiplicationExpression()

         scanner.moveAfterWhitespace()
        val nextChar = scanner.nextChar()

        if (nextChar == '+') {
            scanner.moveForward()
            scanner.moveAfterWhitespace()
            return JavascriptExpression.BooleanOperation(
                    operator = BooleanOperator.Add,
                    lhs = lhs,
                    rhs = parseMultiplicationExpression()
            )
        } else if (nextChar == '-') {
            scanner.moveForward()
            scanner.moveAfterWhitespace()
            return JavascriptExpression.BooleanOperation(
                    operator = BooleanOperator.Subtract,
                    lhs = lhs,
                    rhs = parseMultiplicationExpression()
            )
        }

        return lhs
    }

    private fun parseMultiplicationExpression(): JavascriptExpression {
        val lhs = parseFunctionCall()

        scanner.moveAfterWhitespace()
        val nextChar = scanner.nextChar()

        if (nextChar == '*') {
            scanner.moveForward()
            scanner.moveAfterWhitespace()
            return JavascriptExpression.BooleanOperation(
                operator = BooleanOperator.Multiply,
                lhs = lhs,
                rhs = parseFunctionCall()
            )
        }

        return lhs
    }

    private fun parseFunctionCall(): JavascriptExpression {
        val lhs = parseSimpleExpression()

        if (lhs is JavascriptExpression.Reference) {
            val parameters = mutableListOf<JavascriptExpression>()

            if (scanner.nextChar() != ')') {
                while (scanner.currentChar() != ')') {
                    parameters += parse()
                    scanner.moveAfterWhitespace()
                }

                if (parameters.count() > 1) {
                    scanner.moveForward()
                }
            }

            return JavascriptExpression.FunctionCall(name = lhs.name, parameters = parameters)
        }

        return lhs
    }

    private fun parseSimpleExpression(): JavascriptExpression {
        val literal = scanner.scanUntil { it == ' ' || it == '(' || it == ',' || it == ')' }
        val literalAsDouble = literal.toDoubleOrNull()

        if (literalAsDouble != null) {
            return JavascriptExpression.Literal(value = JavascriptValue.Double(literalAsDouble))
        }

        return JavascriptExpression.Reference(name = literal)
    }
}