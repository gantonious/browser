package ca.antonious.browser.libraries.javascript.parser

import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptProgram
import ca.antonious.browser.libraries.javascript.ast.JavascriptStatement
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.lexer.JavascriptToken
import ca.antonious.browser.libraries.javascript.lexer.JavascriptTokenType
import kotlin.math.max

class JavascriptParser(
    private val tokens: List<JavascriptToken>,
    source: String
) {
    private val sourceLines = source.split("\n")

    companion object {
        private val additiveTokens = setOf(
            JavascriptTokenType.Operator.Plus,
            JavascriptTokenType.Operator.Minus
        )

        private val multiplicativeTokens = setOf(
            JavascriptTokenType.Operator.Multiply,
            JavascriptTokenType.Operator.Divide
        )

        private val comparisonTokens = setOf(
            JavascriptTokenType.Operator.LessThan,
            JavascriptTokenType.Operator.LessThanOrEqual,
            JavascriptTokenType.Operator.GreaterThan,
            JavascriptTokenType.Operator.GreaterThanOrEqual
        )

        private val assignmentToken = setOf(
            JavascriptTokenType.Operator.Assignment,
            JavascriptTokenType.Operator.XorAssign,
            JavascriptTokenType.Operator.MinusAssign,
            JavascriptTokenType.Operator.MinusAssign,
            JavascriptTokenType.Operator.PlusAssign,
            JavascriptTokenType.Operator.MinusAssign,
            JavascriptTokenType.Operator.MultiplyAssign,
            JavascriptTokenType.Operator.DivideAssign
        )

        private val incrementTokens = setOf(
            JavascriptTokenType.PlusPlus,
            JavascriptTokenType.MinusMinus
        )
    }
    private var cursor = 0

    fun parse(): JavascriptProgram {
        val statements = mutableListOf<JavascriptStatement>()

        while (!isAtEnd()) {
            statements += expectStatement()
        }

        return JavascriptProgram(statements)
    }

    private fun expectStatement(): JavascriptStatement {
        maybeConsumeLineTerminator()
        return when (getCurrentToken()) {
            is JavascriptTokenType.Function -> expectFunctionDeclaration()
            is JavascriptTokenType.While -> expectWhileLoop()
            is JavascriptTokenType.If -> expectIfStatement()
            is JavascriptTokenType.Return -> expectReturnStatement()
            is JavascriptTokenType.Let -> expectLetStatement()
            is JavascriptTokenType.Const -> expectConstStatement()
            is JavascriptTokenType.For -> expectForLoop()
            else -> expectExpression()
        }
    }

    private fun expectIfStatement(): JavascriptStatement.IfStatement {
        expectToken<JavascriptTokenType.If>()
        expectToken<JavascriptTokenType.OpenParentheses>()
        val condition = expectExpression()
        expectToken<JavascriptTokenType.CloseParentheses>()

        return JavascriptStatement.IfStatement(
            condition = condition,
            body = expectBlock()
        )
    }

    private fun expectFunctionDeclaration(): JavascriptStatement.Function {
        expectToken<JavascriptTokenType.Function>()

        val functionName = expectToken<JavascriptTokenType.Identifier>()
        expectToken<JavascriptTokenType.OpenParentheses>()

        val parameterNames = mutableListOf<JavascriptTokenType.Identifier>()

        if (getCurrentToken() !is JavascriptTokenType.CloseParentheses) {
            parameterNames += expectToken<JavascriptTokenType.Identifier>()

            while (getCurrentToken() !is JavascriptTokenType.CloseParentheses) {
                expectToken<JavascriptTokenType.Comma>()
                parameterNames += expectToken<JavascriptTokenType.Identifier>()
            }
        }

        expectToken<JavascriptTokenType.CloseParentheses>()

        return JavascriptStatement.Function(
            name = functionName.name,
            parameterNames = parameterNames.map { it.name },
            body = expectBlock()
        )
    }

    private fun expectReturnStatement(): JavascriptStatement.Return {
        expectToken<JavascriptTokenType.Return>()

        return JavascriptStatement.Return(
            expression = expectExpression()
        )
    }

    private fun expectWhileLoop(): JavascriptStatement.WhileLoop {
        expectToken<JavascriptTokenType.While>()

        expectToken<JavascriptTokenType.OpenParentheses>()
        val condition = expectExpression()
        expectToken<JavascriptTokenType.CloseParentheses>()

        maybeConsumeLineTerminator()

        return JavascriptStatement.WhileLoop(
            condition = condition,
            body =  expectBlock()
        )
    }

    private fun expectForLoop(): JavascriptStatement.ForLoop {
        expectToken<JavascriptTokenType.For>()

        expectToken<JavascriptTokenType.OpenParentheses>()
        val initializerExpression = expectExpression()
        expectToken<JavascriptTokenType.SemiColon>()
        val conditionExpression = expectExpression()
        expectToken<JavascriptTokenType.SemiColon>()
        val updaterExpression = expectExpression()
        expectToken<JavascriptTokenType.CloseParentheses>()

        return JavascriptStatement.ForLoop(
            initializerExpression = initializerExpression,
            conditionExpression = conditionExpression,
            updaterExpression = updaterExpression,
            body = expectBlock()
        )
    }

    private fun expectBlock(): JavascriptStatement.Block {
        val statements = mutableListOf<JavascriptStatement>()

        expectToken<JavascriptTokenType.OpenCurlyBracket>()
        maybeConsumeLineTerminator()

        while (getCurrentToken() !is JavascriptTokenType.CloseCurlyBracket) {
            statements += expectStatement()
            maybeConsumeLineTerminator()
        }

        expectToken<JavascriptTokenType.CloseCurlyBracket>()

        return JavascriptStatement.Block(statements)
    }

    private fun expectLetStatement(): JavascriptStatement.LetAssignment {
        expectToken<JavascriptTokenType.Let>()
        val name = expectToken<JavascriptTokenType.Identifier>().name

        if (maybeGetCurrentToken() !is JavascriptTokenType.Operator.Assignment) {
            return JavascriptStatement.LetAssignment(
                name = name,
                expression = JavascriptExpression.Literal(value = JavascriptValue.Undefined)
            )
        }

        expectToken<JavascriptTokenType.Operator.Assignment>()

        return JavascriptStatement.LetAssignment(
            name = name,
            expression = expectExpression()
        )
    }

    private fun expectConstStatement(): JavascriptStatement.ConstAssignment {
        expectToken<JavascriptTokenType.Const>()
        val name = expectToken<JavascriptTokenType.Identifier>().name
        expectToken<JavascriptTokenType.Operator.Assignment>()

        return JavascriptStatement.ConstAssignment(
            name = name,
            expression = expectExpression()
        )
    }

    private fun expectExpression(): JavascriptExpression {
        return expectAssignmentExpression()
    }

    private fun expectAssignmentExpression(): JavascriptExpression {
        var expression = expectComparisonExpression()

        while (maybeGetCurrentToken() in assignmentToken) {
            expression = JavascriptExpression.BinaryOperation(
                operator = expectToken(),
                lhs = expression,
                rhs = expectComparisonExpression()
            )
        }

        return expression
    }

    private fun expectComparisonExpression(): JavascriptExpression {
        var expression = expectAdditiveExpression()

        while (maybeGetCurrentToken() in comparisonTokens) {
            expression = JavascriptExpression.BinaryOperation(
                operator = expectToken(),
                lhs = expression,
                rhs = expectAdditiveExpression()
            )
        }

        return expression
    }

    private fun expectAdditiveExpression(): JavascriptExpression {
        var expression = expectMultiplicativeExpression()

        while (maybeGetCurrentToken() in additiveTokens) {
            expression = JavascriptExpression.BinaryOperation(
                operator = expectToken(),
                lhs = expression,
                rhs = expectMultiplicativeExpression()
            )
        }

        return expression
    }

    private fun expectMultiplicativeExpression(): JavascriptExpression {
        var expression = expectPostfixIncrementExpression()

        while (maybeGetCurrentToken() in multiplicativeTokens) {
            expression = JavascriptExpression.BinaryOperation(
                operator = expectToken(),
                lhs = expression,
                rhs = expectPostfixExpression()
            )
        }

        return expression
    }

    private fun expectPostfixIncrementExpression(): JavascriptExpression {
        val expression = expectPostfixExpression()

        if (maybeGetCurrentToken() in incrementTokens) {
            return JavascriptExpression.UnaryOperation(
                operator = expectToken(),
                expression = expression,
                isPrefix = false
            )
        }

        return expression
    }

    private fun expectPostfixExpression(): JavascriptExpression {
        var expression = expectSimpleExpression()
        var continueParsing = true

        loop@while (continueParsing) {
            expression = when (maybeGetCurrentToken()) {
                is JavascriptTokenType.OpenParentheses -> expectFunctionCallOn(expression)
                is JavascriptTokenType.OpenBracket -> expectIndexAccessOn(expression)
                is JavascriptTokenType.Dot -> expectDotAccessOn(expression)
                else -> {
                    continueParsing = false
                    expression
                }
            }
        }

        return expression
    }

    private fun expectFunctionCallOn(expression: JavascriptExpression): JavascriptExpression.FunctionCall {
        expectToken<JavascriptTokenType.OpenParentheses>()
        val arguments = mutableListOf<JavascriptExpression>()

        if (getCurrentToken() !is JavascriptTokenType.CloseParentheses) {
            arguments += expectExpression()

            while (getCurrentToken() !is JavascriptTokenType.CloseParentheses) {
                expectToken<JavascriptTokenType.Comma>()
                arguments += expectExpression()
            }
        }

        expectToken<JavascriptTokenType.CloseParentheses>()

        return JavascriptExpression.FunctionCall(
            expression = expression,
            parameters = arguments
        )
    }

    private fun expectIndexAccessOn(expression: JavascriptExpression): JavascriptExpression.IndexAccess {
        expectToken<JavascriptTokenType.OpenBracket>()
        val index = expectExpression()
        expectToken<JavascriptTokenType.CloseBracket>()

        return JavascriptExpression.IndexAccess(
            indexExpression = index,
            expression = expression
        )
    }

    private fun expectDotAccessOn(expression: JavascriptExpression): JavascriptExpression.DotAccess {
        expectToken<JavascriptTokenType.Dot>();

        return JavascriptExpression.DotAccess(
            expression = expression,
            propertyName = expectToken<JavascriptTokenType.Identifier>().name
        )
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
            is JavascriptTokenType.Identifier -> {
                advanceCursor()
                JavascriptExpression.Reference(name = currentToken.name)
            }
            else -> throwUnexpectedTokenFound()
        }
    }

    private fun advanceCursor() {
        cursor += 1
    }

    private inline fun <reified T: JavascriptTokenType> expectToken(): T {
        if (getCurrentToken() !is T) {
            throwUnexpectedTokenFound()
        }

        return (getCurrentToken() as T).also {
            advanceCursor()
        }
    }

    private fun throwUnexpectedTokenFound(): Nothing {
        val sourceInfo = tokens[cursor].sourceInfo
        val topLine = "Uncaught SyntaxError: Unexpected token '${getCurrentToken()}'"
        val errorLines = sourceLines.subList(max(0, sourceInfo.line - 3), sourceInfo.line + 1)

        val message = "$topLine\n${errorLines.joinToString("\n")}\n${" ".repeat(sourceInfo.column)}^"

        throw UnexpectedTokenException(message)
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

    private fun maybeGetCurrentToken(): JavascriptTokenType? {
        if (isAtEnd()) {
            return null
        }
        return tokens[cursor].type
    }

    private fun isAtEnd(): Boolean {
        return cursor >= tokens.size
    }

    class UnexpectedTokenException(message: String) : Exception(message)
    class UnexpectedEndOfFileException() : Exception("Uncaught SyntaxError: Unexpected eof.")
}