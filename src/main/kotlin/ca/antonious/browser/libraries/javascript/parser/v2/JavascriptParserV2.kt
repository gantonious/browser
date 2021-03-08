package ca.antonious.browser.libraries.javascript.parser.v2

import ca.antonious.browser.libraries.javascript.lexer.JavascriptToken
import ca.antonious.browser.libraries.javascript.lexer.JavascriptTokenType

class JavascriptParserV2(private val tokens: List<JavascriptToken>) {

    companion object {
        private val additiveTokens = setOf(
            JavascriptTokenType.Plus,
            JavascriptTokenType.Minus
        )

        private val multiplicativeTokens = setOf(
            JavascriptTokenType.Multiply,
            JavascriptTokenType.Divide
        )
    }
    private var cursor = 0

    fun parse(): JavascriptProgram {
        val statements = mutableListOf<JavascriptNode>()

        while (!isAtEnd()) {
            expectStatement()
        }

        return JavascriptProgram(statements)
    }

    private fun expectStatement(): JavascriptNode {
        maybeConsumeLineTerminator()
        return when (val currentToken = getCurrentToken()) {
            is JavascriptTokenType.Function -> expectFunctionDeclaration()
            is JavascriptTokenType.While -> expectWhileLoop()
            else -> throw UnexpectedTokenException(tokens[cursor])
        }
    }

    private fun expectFunctionDeclaration(): JavascriptNode.Function {
        val functionName = expectToken<JavascriptTokenType.Identifier>()
        expectToken<JavascriptTokenType.OpenBracket>()

        val parameterNames = mutableListOf<JavascriptTokenType.Identifier>()

        if (getCurrentToken() !is JavascriptTokenType.CloseBracket) {
            parameterNames += expectToken<JavascriptTokenType.Identifier>()

            while (getCurrentToken() !is JavascriptTokenType.CloseBracket) {
                expectToken<JavascriptTokenType.Comma>()
                parameterNames += expectToken<JavascriptTokenType.Identifier>()
            }
        }

        expectToken<JavascriptTokenType.CloseBracket>()

        return JavascriptNode.Function(
            name = functionName.name,
            parameterNames = parameterNames.map { it.name },
            body = expectBlock()
        )
    }

    private fun expectWhileLoop(): JavascriptNode.WhileLoop {
        expectToken<JavascriptTokenType.OpenBracket>()
        val condition = expectExpression()
        expectToken<JavascriptTokenType.CloseBracket>()

        maybeConsumeLineTerminator()

        return JavascriptNode.WhileLoop(
            condition = condition,
            body =  expectBlock()
        )
    }

    private fun expectBlock(): JavascriptNode.Block {
        val statements = mutableListOf<JavascriptNode>()

        expectToken<JavascriptTokenType.OpenCurlyBracket>()
        maybeConsumeLineTerminator()

        while (getCurrentToken() !is JavascriptTokenType.CloseCurlyBracket) {
            statements += expectStatement()
            maybeConsumeLineTerminator()
        }

        expectToken<JavascriptTokenType.CloseCurlyBracket>()

        return JavascriptNode.Block(statements)
    }

    private fun expectExpression(): JavascriptExpression {
        return expectAdditiveExpression()
    }

    private fun expectAssignmentExpression(): JavascriptExpression {
        val lhs = expectAdditiveExpression()

        when (val currentToken = getCurrentToken()) {
            is JavascriptTokenType.Assignment,
            is JavascriptTokenType.PlusAssign,
            is JavascriptTokenType.MinusAssign,
            is JavascriptTokenType.MultiplyAssign,
            is JavascriptTokenType.DivideAssign,
            is JavascriptTokenType.AndAssign,
            is JavascriptTokenType.OrAssign,
            is JavascriptTokenType.XorAssign,
            is JavascriptTokenType.ModAssign -> {
                // return assignment expression
            }
        }

        return lhs
    }

    private fun expectAdditiveExpression(): JavascriptExpression {
        var expression = expectMultiplicativeExpression()

        while (getCurrentToken() in additiveTokens) {
            expression = JavascriptExpression.BinaryOperation(
                operator = getCurrentToken().also { advanceCursor() },
                lhs = expression,
                rhs = expectMultiplicativeExpression()
            )
        }

        return expression
    }

    private fun expectMultiplicativeExpression(): JavascriptExpression {
        var expression = expectSimpleExpression()

        while (getCurrentToken() in multiplicativeTokens) {
            advanceCursor()
            expression = JavascriptExpression.BinaryOperation(
                operator = getCurrentToken().also { advanceCursor() },
                lhs = expression,
                rhs = expectSimpleExpression()
            )
        }

        return expression
    }

    private fun expectSimpleExpression(): JavascriptExpression {
        return when (val currentToken = getCurrentToken()) {
            is JavascriptTokenType.Number -> {
                advanceCursor()
                JavascriptExpression.Literal(value = JavascriptValue.Number(currentToken.value))
            }
            is JavascriptTokenType.String -> {
                advanceCursor()
                JavascriptExpression.Literal(value = JavascriptValue.String(currentToken.value))
            }
            is JavascriptTokenType.Boolean -> {
                advanceCursor()
                JavascriptExpression.Literal(value = JavascriptValue.Boolean(currentToken.value))
            }
            is JavascriptTokenType.Undefined -> {
                advanceCursor()
                JavascriptExpression.Literal(value = JavascriptValue.Undefined)
            }
            else -> error("")
        }
    }

    private fun advanceCursor() {
        cursor += 1
    }

    private inline fun <reified T: JavascriptTokenType> expectToken(): T {
        if (getCurrentToken() !is T) {
            throw UnexpectedTokenException(tokens[cursor])
        }

        return (getCurrentToken() as T).also {
            advanceCursor()
        }
    }

    private inline fun <reified T: JavascriptTokenType> tryGetToken(): T? {
        if (getCurrentToken() !is T) {
            return null
        }

        return (getCurrentToken() as? T).also {
            advanceCursor()
        }
    }

    private inline fun maybeConsumeLineTerminator() {
        tryGetToken<JavascriptTokenType.SemiColon>()
    }

    private fun getCurrentToken(): JavascriptTokenType {
        if (isAtEnd()) {
            throw UnexpectedEndOfFileException()
        }
        return tokens[cursor].type
    }

    private fun isAtEnd(): Boolean {
        return cursor >= tokens.size
    }

    class UnexpectedTokenException(token: JavascriptToken) : Exception("Uncaught SyntaxError: Unexpected token '${token.type}'")
    class UnexpectedEndOfFileException() : Exception("Uncaught SyntaxError: Unexpected eof.'")
}