package ca.antonious.browser.libraries.javascript.parser

import ca.antonious.browser.libraries.javascript.ast.JavascriptBooleanOperator
import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptNode
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

class JavascriptParser {
    fun parse(rawJavascript: String): List<JavascriptNode> {
        val scanner = StringScanner(rawJavascript)
        val body = mutableListOf<JavascriptNode>()

        while (!scanner.isAtEnd) {
            scanner.moveAfterWhitespace()

            when (val nextWord = scanner.scanWhile { it.isLetter() }) {
                "function" -> {
                    val name = scanner.scanUntil(char ='(')
                    val params = scanner.scanUntil(char = ')')
                    scanner.scanUntil('{')
                    val block = scanner.scanUntil('}', balancedAgainst = '{')
                    body += JavascriptNode.Function(name = name, body = parse(block), parameterNames = params.split(",").filter { it.isNotEmpty() }.map { it.trim() })
                }
                "return" -> {
                    val rawReturnExpression = scanner.scanUntil { it == ';' || it == '\n' }
                    body += JavascriptNode.Return(expression = ExpressionParser(rawReturnExpression).parse())
                }
                "if" -> {
                    scanner.scanUntil(char = '(')
                    val expression = scanner.scanUntil(char = ')')
                    scanner.scanUntil('{')
                    val ifBody = scanner.scanUntil('}', balancedAgainst = '{')

                    body += JavascriptNode.IfStatement(
                            condition = ExpressionParser(expression).parse(),
                            body = parse(ifBody)
                    )
                }
                "let" -> {
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
                }
                "while" -> {
                    scanner.moveAfterWhitespace()
                    scanner.scanUntil('(')
                    val condition = scanner.scanUntil(char = ')')
                    scanner.scanUntil('{')
                    val whileBody = scanner.scanUntil('}', balancedAgainst = '{')

                    body += JavascriptNode.WhileLoop(
                            condition = ExpressionParser(condition).parse(),
                            body = parse(whileBody)
                    )
                }
                else -> {
                    if (nextWord.isNotEmpty()) {
                        scanner.moveBack()
                        body += ExpressionParser(nextWord + scanner.scanUntil { it == ';' || it == '\n' }).parse()
                    }
                }
            }
        }

        return body
    }
}