package ca.antonious.browser.libraries.javascript.parser

import ca.antonious.browser.libraries.javascript.ast.JavascriptBooleanOperator
import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

class ExpressionParser(expression: String) {
    private val scanner = StringScanner(expression)

    fun parse(): JavascriptExpression {
        return parseComparisonExpression()
    }

    private fun parseComparisonExpression(): JavascriptExpression {
        val lhs = parseAdditionExpression()

        scanner.moveAfterWhitespace()
        val nextChar = scanner.nextChar()

        if (nextChar == '<') {
            scanner.moveForward()
            scanner.moveAfterWhitespace()
            return JavascriptExpression.BooleanOperation(
                operator = JavascriptBooleanOperator.LessThan,
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
                operator = JavascriptBooleanOperator.Add,
                lhs = lhs,
                rhs = parseMultiplicationExpression()
            )
        } else if (nextChar == '-') {
            scanner.moveForward()
            scanner.moveAfterWhitespace()
            return JavascriptExpression.BooleanOperation(
                operator = JavascriptBooleanOperator.Subtract,
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
                operator = JavascriptBooleanOperator.Multiply,
                lhs = lhs,
                rhs = parseFunctionCall()
            )
        }

        return lhs
    }

    private fun parseDotAccess(): JavascriptExpression {
        val lhs = parseSimpleExpression()
        scanner.moveAfterWhitespace()

        if (scanner.nextChar() == '.') {
            scanner.moveForward()
            val propertyName = scanner.scanWhile(moveAfter = false) { it.isLetterOrDigit() }
            return JavascriptExpression.DotAccess(propertyName = propertyName, expression = lhs)
        }

        return lhs
    }

    private fun parseFunctionCall(): JavascriptExpression {
        val lhs = parseDotAccess()
        scanner.moveAfterWhitespace()

        if (scanner.nextChar() == '(') {
            val parameters = mutableListOf<JavascriptExpression>()

            // Consume '(
            scanner.moveForward()

            while (scanner.nextChar() != ')') {
                scanner.moveAfterWhitespace()
                parameters += parse()
                scanner.moveAfterWhitespace()
                if (scanner.nextChar() == ',') {
                    scanner.moveForward()
                }
            }

            scanner.moveForward()

            return JavascriptExpression.FunctionCall(expression = lhs, parameters = parameters)
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
            return JavascriptExpression.Literal(value = JavascriptValue.Number(literalAsDouble))
        }

        return when (literal) {
            "true" -> JavascriptExpression.Literal(value = JavascriptValue.Boolean(true))
            "false" -> JavascriptExpression.Literal(value = JavascriptValue.Boolean(false))
            "undefined" -> JavascriptExpression.Literal(value = JavascriptValue.Undefined)
            else -> JavascriptExpression.Reference(name = literal)
        }
    }
}