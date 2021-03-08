package ca.antonious.browser.libraries.javascript.parser

import ca.antonious.browser.libraries.javascript.ast.JavascriptStatement

class JavascriptParser {
    fun parse(rawJavascript: String): List<JavascriptStatement> {
        val scanner = StringScanner(rawJavascript)
        val body = mutableListOf<JavascriptStatement>()

        while (!scanner.isAtEnd) {
            scanner.moveAfterWhitespace()

            when (val nextWord = scanner.scanWhile { it.isLetter() }) {
                "function" -> {
                    val name = scanner.scanUntil(char ='(')
                    val params = scanner.scanUntil(char = ')')
                    scanner.scanUntil('{')
                    val block = scanner.scanUntil('}', balancedAgainst = '{')
                    body += JavascriptStatement.Function(name = name, body = JavascriptStatement.Block(parse(block)), parameterNames = params.split(",").filter { it.isNotEmpty() }.map { it.trim() })
                }
                "return" -> {
                    val rawReturnExpression = scanner.scanUntil { it == ';' || it == '\n' }
                    body += JavascriptStatement.Return(expression = ExpressionParser(rawReturnExpression).parse())
                }
                "if" -> {
                    scanner.scanUntil(char = '(')
                    val expression = scanner.scanUntil(char = ')')
                    scanner.scanUntil('{')
                    val ifBody = scanner.scanUntil('}', balancedAgainst = '{')

                    body += JavascriptStatement.IfStatement(
                            condition = ExpressionParser(expression).parse(),
                            body = JavascriptStatement.Block(parse(ifBody))
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

                    body += JavascriptStatement.LetAssignment(
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

                    body += JavascriptStatement.WhileLoop(
                            condition = ExpressionParser(condition).parse(),
                            body = JavascriptStatement.Block(parse(whileBody))
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