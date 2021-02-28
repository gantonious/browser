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
            val nextWord = scanner.scanWhile { it.isLetter() }

            if (nextWord == "function") {
                val name = scanner.scanUntil(char ='(')
                val params = scanner.scanUntil(char = ')')
                scanner.scanUntil('{')
                val block = scanner.scanUntil('}', balancedAgainst = '{')
                body += JavascriptNode.Function(name = name, body = parse(block), parameterNames = params.split(",").filter { it.isNotEmpty() }.map { it.trim() })
            } else if (nextWord == "return") {
                val rawReturnExpression = scanner.scanUntil { it == ';' || it == '\n' }
                body += JavascriptNode.Return(expression = ExpressionParser(rawReturnExpression).parse())
            } else if (nextWord == "if") {
                scanner.scanUntil(char = '(')
                val expression = scanner.scanUntil(char = ')')
                scanner.scanUntil('{')
                val ifBody = scanner.scanUntil('}', balancedAgainst = '{')

                body += JavascriptNode.IfStatement(
                    expression = ExpressionParser(expression).parse(),
                    body = parse(ifBody)
                )

            } else if (nextWord == "let") {
                scanner.moveAfterWhitespace()
                val variableName = scanner.scanUntil { it == '=' || it == ' ' }
                if (scanner.nextChar() != '=') {
                    scanner.scanUntil { it == '=' }
                } else if (scanner.nextChar() == '=') {
                    scanner.moveForward()
                }

                scanner.moveAfterWhitespace()
                val variableExpression = scanner.scanUntil { it == ';' || it == '\n' }

                body += JavascriptNode.LetAssignment(
                    name = variableName,
                    expression = ExpressionParser(variableExpression).parse()
                )
            } else if (nextWord == "while") {
                scanner.moveAfterWhitespace()
                scanner.scanUntil('(')
                val condition = scanner.scanUntil(char = ')')
                scanner.scanUntil('{')
                val whileBody = scanner.scanUntil('}', balancedAgainst = '{')

                body += JavascriptNode.WhileLoop(
                    condition = ExpressionParser(condition).parse(),
                    body = parse(whileBody)
                )
            } else if (nextWord.isNotEmpty()) {
                scanner.moveBack()
                body += ExpressionParser(nextWord + scanner.scanUntil { it == ';' || it == '\n' }).parse()
            }
        }

        return body
    }
}

class ExpressionParser(expression: String) {
    private val scanner = StringScanner(expression)

    fun parse(): JavascriptExpression {
       return parseComparisionExpression()
    }

    private fun parseComparisionExpression(): JavascriptExpression {
        val lhs = parseAdditionExpression()

        scanner.moveAfterWhitespace()
        val nextChar = scanner.nextChar()

        if (nextChar == '<') {
            scanner.moveForward()
            scanner.moveAfterWhitespace()
            return JavascriptExpression.BooleanOperation(
                operator = BooleanOperator.LessThan,
                lhs = lhs,
                rhs = parseMultiplicationExpression()
            )
        }

        return lhs
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

    private fun parseDotAccess(): JavascriptExpression {
        val lhs = parseSimpleExpression()

        if (lhs is JavascriptExpression.Reference) {
            if (scanner.nextChar() != '.') {
                return lhs
            }

            scanner.moveForward()

            val propertyName = scanner.scanWhile { it.isLetterOrDigit() }

            return JavascriptExpression.DotAccess(propertyName = propertyName, expression = lhs)
        }

        return lhs
    }

    private fun parseFunctionCall(): JavascriptExpression {
        val lhs = parseDotAccess()

        if (lhs is JavascriptExpression.Reference) {
            val parameters = mutableListOf<JavascriptExpression>()

            if (scanner.nextChar() != '(') {
                return lhs
            }
            // Consume '(
            scanner.moveForward()

            while (scanner.nextChar() != ')') {
                parameters += parse()
                scanner.moveAfterWhitespace()
            }

            scanner.moveForward()

            return JavascriptExpression.FunctionCall(name = lhs.name, parameters = parameters)
        }

        return lhs
    }

    private fun parseSimpleExpression(): JavascriptExpression {
        if (scanner.nextChar() == '"') {
            scanner.moveForward()
            val string = scanner.scanUntil('"')
            return JavascriptExpression.Literal(value = JavascriptValue.String(string))
        }

        val literal = scanner.scanWhile(moveAfter = false) { it.isLetterOrDigit() }
        val literalAsDouble = literal.toDoubleOrNull()

        if (literalAsDouble != null) {
            return JavascriptExpression.Literal(value = JavascriptValue.Double(literalAsDouble))
        }

        if (literal == "true") {
            return JavascriptExpression.Literal(value = JavascriptValue.Boolean(true))
        }

        if (literal == "false") {
            return JavascriptExpression.Literal(value = JavascriptValue.Boolean(false))
        }

        if (literal == "undefined") {
            return JavascriptExpression.Literal(value = JavascriptValue.Undefined)
        }

        return JavascriptExpression.Reference(name = literal)
    }
}