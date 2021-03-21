package ca.antonious.browser.libraries.javascript.parser

import ca.antonious.browser.libraries.javascript.ast.*
import ca.antonious.browser.libraries.javascript.interpreter.builtins.regex.JavascriptRegex
import ca.antonious.browser.libraries.javascript.lexer.JavascriptToken
import ca.antonious.browser.libraries.javascript.lexer.JavascriptTokenType
import java.util.Stack
import kotlin.math.max

class JavascriptParser(
    private val tokens: List<JavascriptToken>,
    source: String,
    val sourceFileName: String = ""
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
            JavascriptTokenType.Operator.GreaterThanOrEqual,
            JavascriptTokenType.In
        )

        private val assignmentToken = setOf(
            JavascriptTokenType.Operator.Assignment,
            JavascriptTokenType.Operator.XorAssign,
            JavascriptTokenType.Operator.MinusAssign,
            JavascriptTokenType.Operator.MinusAssign,
            JavascriptTokenType.Operator.PlusAssign,
            JavascriptTokenType.Operator.MinusAssign,
            JavascriptTokenType.Operator.MultiplyAssign,
            JavascriptTokenType.Operator.DivideAssign,
            JavascriptTokenType.Operator.OrAssign,
            JavascriptTokenType.Operator.AndAssign
        )

        private val incrementTokens = setOf(
            JavascriptTokenType.PlusPlus,
            JavascriptTokenType.MinusMinus
        )

        private val prefixTokens = setOf(
            JavascriptTokenType.PlusPlus,
            JavascriptTokenType.MinusMinus,
            JavascriptTokenType.Operator.Not,
            JavascriptTokenType.Operator.BitNot,
            JavascriptTokenType.Operator.Plus,
            JavascriptTokenType.Operator.Minus,
            JavascriptTokenType.TypeOf,
            JavascriptTokenType.Void
        )

        private val equalityTokens = setOf(
            JavascriptTokenType.Operator.Equals,
            JavascriptTokenType.Operator.NotEquals,
            JavascriptTokenType.Operator.StrictEquals,
            JavascriptTokenType.Operator.StrictNotEquals
        )

        private val rightToLeftAssociativeOperators = assignmentToken
    }

    private var cursor = 0
    private var savedCursor = 0

    fun parse(): JavascriptProgram {
        val statements = mutableListOf<JavascriptStatement>()

        while (!isAtEnd()) {
            if (getCurrentToken() is JavascriptTokenType.SemiColon) {
                advanceCursor()
                continue
            } else {
                statements += expectStatement()
            }
        }

        return JavascriptProgram(statements)
    }

    private fun expectStatement(): JavascriptStatement {
        return when (getCurrentToken()) {
            is JavascriptTokenType.Function -> expectFunctionDeclaration()
            is JavascriptTokenType.While -> expectWhileLoop()
            is JavascriptTokenType.If -> expectIfStatement()
            is JavascriptTokenType.Return -> expectReturnStatement()
            is JavascriptTokenType.Var -> expectVarStatement()
            is JavascriptTokenType.Let -> expectLetStatement()
            is JavascriptTokenType.Const -> expectConstStatement()
            is JavascriptTokenType.For -> expectForLoop()
            is JavascriptTokenType.Do -> expectDoWhileLoop()
            is JavascriptTokenType.Identifier -> expectLabeledStatement()
            is JavascriptTokenType.OpenCurlyBracket -> expectBlock()
            is JavascriptTokenType.Try -> expectTryStatement()
            is JavascriptTokenType.Throw -> expectThrowStatement()
            else -> expectExpression()
        }
    }

    private fun expectThrowStatement(): JavascriptStatement {
        expectToken<JavascriptTokenType.Throw>()
        return JavascriptStatement.Throw(expression = expectExpression())
    }

    private fun expectTryStatement(): JavascriptStatement {
        var errorName: String? = null
        var catchBlock: JavascriptStatement.Block? = null
        var finallyBlock: JavascriptStatement.Block? = null

        expectToken<JavascriptTokenType.Try>()
        val tryBlock = expectBlock()

        when (getCurrentToken()) {
            is JavascriptTokenType.Catch -> {
                expectToken<JavascriptTokenType.Catch>()

                if (getCurrentToken() is JavascriptTokenType.OpenParentheses) {
                    expectToken<JavascriptTokenType.OpenParentheses>()
                    errorName = expectToken<JavascriptTokenType.Identifier>().name
                    expectToken<JavascriptTokenType.CloseParentheses>()
                }

                catchBlock = expectBlock()
            }
            is JavascriptTokenType.Finally -> {
                finallyBlock = expectFinallyBlock()
            }
            else -> throwUnexpectedTokenFound()
        }

        if (maybeGetCurrentToken() is JavascriptTokenType.Finally) {
            finallyBlock = expectFinallyBlock()
        }

        return JavascriptStatement.TryStatement(
            tryBlock = tryBlock,
            catchBlock = catchBlock,
            errorName = errorName,
            finallyBlock = finallyBlock
        )
    }

    private fun expectFinallyBlock(): JavascriptStatement.Block {
        expectToken<JavascriptTokenType.Finally>()
        return expectBlock()
    }

    private fun expectLabeledStatement(): JavascriptStatement {
        return if (maybeGetNextToken() is JavascriptTokenType.Colon) {
            val label = expectToken<JavascriptTokenType.Identifier>()
            expectToken<JavascriptTokenType.Colon>()
            return JavascriptStatement.LabeledStatement(label = label.name, statement = expectStatement())
        } else {
            expectExpression()
        }
    }

    private fun expectIfStatement(): JavascriptStatement.IfStatement {
        val conditions = mutableListOf<JavascriptStatement.IfStatement.ConditionAndStatement>()

        expectToken<JavascriptTokenType.If>()
        expectToken<JavascriptTokenType.OpenParentheses>()
        val mainCondition = expectExpression()
        expectToken<JavascriptTokenType.CloseParentheses>()

        conditions += JavascriptStatement.IfStatement.ConditionAndStatement(
            condition = mainCondition,
            body = expectBlockOrStatement()
        )

        while (maybeGetCurrentToken() is JavascriptTokenType.Else) {
            expectToken<JavascriptTokenType.Else>()

            conditions += when (getCurrentToken()) {
                JavascriptTokenType.If -> {
                    expectToken<JavascriptTokenType.If>()
                    expectToken<JavascriptTokenType.OpenParentheses>()
                    val elseIfCondition = expectExpression()
                    expectToken<JavascriptTokenType.CloseParentheses>()

                    JavascriptStatement.IfStatement.ConditionAndStatement(
                        condition = elseIfCondition,
                        body = expectBlockOrStatement()
                    )
                }
                else -> {
                    JavascriptStatement.IfStatement.ConditionAndStatement(
                        condition = JavascriptExpression.Literal(JavascriptValue.Boolean(true)),
                        body = expectBlockOrStatement()
                    )
                }
            }
        }

        return JavascriptStatement.IfStatement(conditions = conditions)
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

        return when (maybeGetCurrentToken()) {
            is JavascriptTokenType.SemiColon -> {
                advanceCursor()
                JavascriptStatement.Return(expression = null)
            }
            is JavascriptTokenType.CloseCurlyBracket, null -> {
                JavascriptStatement.Return(expression = null)
            }
            else -> {
                JavascriptStatement.Return(expression = expectExpression())
            }
        }
    }

    private fun expectWhileLoop(): JavascriptStatement.WhileLoop {
        expectToken<JavascriptTokenType.While>()

        expectToken<JavascriptTokenType.OpenParentheses>()
        val condition = expectExpression()
        expectToken<JavascriptTokenType.CloseParentheses>()

        maybeConsumeLineTerminator()

        return JavascriptStatement.WhileLoop(
            condition = condition,
            body = expectBlock()
        )
    }

    private fun expectDoWhileLoop(): JavascriptStatement {
        expectToken<JavascriptTokenType.Do>()
        val body = expectBlockOrStatement()
        expectToken<JavascriptTokenType.While>()
        expectToken<JavascriptTokenType.OpenParentheses>()
        val condition = expectExpression()
        expectToken<JavascriptTokenType.CloseParentheses>()

        return JavascriptStatement.DoWhileLoop(body = body, condition = condition)
    }

    private fun expectForLoop(): JavascriptStatement {
        expectToken<JavascriptTokenType.For>()
        expectToken<JavascriptTokenType.OpenParentheses>()

        if (getCurrentToken() is JavascriptTokenType.SemiColon) {
            return expectCStyleForLoop(initializerStatement = null)
        }

        checkpoint()
        val forExpression = expectStatement()

        if (forExpression is JavascriptExpression.BinaryOperation && forExpression.operator is JavascriptTokenType.In) {
            expectToken<JavascriptTokenType.CloseParentheses>()
            return JavascriptStatement.ForEachLoop(
                initializerStatement = forExpression.lhs,
                enumerableExpression = forExpression.rhs,
                body = expectBlockOrStatement()
            )
        }

        revertToCheckpoint()

        val initializerStatement = expectStatement()

        return when (getCurrentToken()) {
            is JavascriptTokenType.SemiColon -> expectCStyleForLoop(initializerStatement)
            is JavascriptTokenType.In -> expectForEachLoop(initializerStatement)
            else -> throwUnexpectedTokenFound()
        }
    }

    private fun expectCStyleForLoop(initializerStatement: JavascriptStatement?): JavascriptStatement {
        expectToken<JavascriptTokenType.SemiColon>()

        val conditionExpression = if (getCurrentToken() is JavascriptTokenType.SemiColon) {
            null
        } else {
            expectExpression()
        }

        expectToken<JavascriptTokenType.SemiColon>()

        val updaterExpression = if (getCurrentToken() is JavascriptTokenType.CloseParentheses) {
            null
        } else {
            expectExpression()
        }

        expectToken<JavascriptTokenType.CloseParentheses>()

        return JavascriptStatement.ForLoop(
            initializerStatement = initializerStatement,
            conditionExpression = conditionExpression,
            updaterExpression = updaterExpression,
            body = expectBlockOrStatement()
        )
    }

    private fun expectForEachLoop(initializerStatement: JavascriptStatement): JavascriptStatement {
        expectToken<JavascriptTokenType.In>()
        val enumerableExpression = expectExpression()
        expectToken<JavascriptTokenType.CloseParentheses>()

        return JavascriptStatement.ForEachLoop(
            initializerStatement = initializerStatement,
            enumerableExpression = enumerableExpression,
            body = expectBlockOrStatement()
        )
    }

    private fun expectBlockOrStatement(): JavascriptStatement {
        return if (getCurrentToken() is JavascriptTokenType.OpenCurlyBracket) {
            expectBlock()
        } else {
            expectStatement().also { maybeConsumeLineTerminator() }
        }
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

    private fun expectVarStatement(): JavascriptStatement.VarAssignment {
        expectToken<JavascriptTokenType.Var>()
        return JavascriptStatement.VarAssignment(assignments = expectAssignmentStatements())
    }

    private fun expectLetStatement(): JavascriptStatement.LetAssignment {
        expectToken<JavascriptTokenType.Let>()
        return JavascriptStatement.LetAssignment(assignments = expectAssignmentStatements())
    }

    private fun expectConstStatement(): JavascriptStatement.ConstAssignment {
        expectToken<JavascriptTokenType.Const>()
        return JavascriptStatement.ConstAssignment(assignments = expectAssignmentStatements())
    }

    private fun expectAssignmentStatements(): List<AssignmentStatement> {
        val statements = mutableListOf<AssignmentStatement>()

        loop@ while (true) {
            when (maybeGetCurrentToken()) {
                is JavascriptTokenType.Comma -> expectToken<JavascriptTokenType.Comma>()
                is JavascriptTokenType.Identifier -> {
                    val identifier = expectToken<JavascriptTokenType.Identifier>()

                    statements += if (maybeGetCurrentToken() is JavascriptTokenType.Operator.Assignment) {
                        expectToken<JavascriptTokenType.Operator.Assignment>()
                        AssignmentStatement(name = identifier.name, expression = expectSubExpression())
                    } else {
                        AssignmentStatement(name = identifier.name, expression = null)
                    }

                    if (maybeGetCurrentToken() !is JavascriptTokenType.Comma) {
                        break@loop
                    }
                }
                else -> break@loop
            }
        }

        return statements
    }

    private fun expectExpression(): JavascriptExpression {
        return expectCommaExpression()
    }

    private fun expectSubExpression(): JavascriptExpression {
        return expectTernaryExpression()
    }

    private fun expectCommaExpression(): JavascriptExpression {
        var expression = expectTernaryExpression()

        while (maybeGetCurrentToken() is JavascriptTokenType.Comma) {
            expression = JavascriptExpression.BinaryOperation(
                operator = expectToken(),
                lhs = expression,
                rhs = expectTernaryExpression()
            )
        }

        return expression
    }

    private fun expectTernaryExpression(): JavascriptExpression {
        val expression = expectBitwiseAndExpression()

        if (maybeGetCurrentToken() is JavascriptTokenType.QuestionMark) {
            expectToken<JavascriptTokenType.QuestionMark>()
            return JavascriptExpression.TernaryOperation(
                condition = expression,
                ifTruthy = expectTernaryExpression().also { expectToken<JavascriptTokenType.Colon>() },
                ifNot = expectTernaryExpression()
            )
        }

        return expression
    }

    private fun expectBitwiseAndExpression(): JavascriptExpression {
        var expression = expectAssignmentExpression()

        while (maybeGetCurrentToken() is JavascriptTokenType.Operator.And) {
            expression = JavascriptExpression.BinaryOperation(
                operator = expectToken(),
                lhs = expression,
                rhs = expectAssignmentExpression()
            )
        }

        return expression
    }

    private fun expectAssignmentExpression(): JavascriptExpression {
        var expression = expectLogicalOrExpression()

        while (maybeGetCurrentToken() in assignmentToken) {
            expression = JavascriptExpression.BinaryOperation(
                operator = expectToken(),
                lhs = expression,
                rhs = expectLogicalOrExpression()
            )
        }

        return expression.convertToRightToLeftAssociativity()
    }

    private fun expectLogicalOrExpression(): JavascriptExpression {
        var expression = expectLogicalAndExpression()

        while (maybeGetCurrentToken() is JavascriptTokenType.Operator.OrOr) {
            expression = JavascriptExpression.BinaryOperation(
                operator = expectToken(),
                lhs = expression,
                rhs = expectLogicalAndExpression()
            )
        }

        return expression
    }

    private fun expectLogicalAndExpression(): JavascriptExpression {
        var expression = expectEqualityExpression()

        while (maybeGetCurrentToken() is JavascriptTokenType.Operator.AndAnd) {
            expression = JavascriptExpression.BinaryOperation(
                operator = expectToken(),
                lhs = expression,
                rhs = expectEqualityExpression()
            )
        }

        return expression
    }

    private fun expectEqualityExpression(): JavascriptExpression {
        var expression = expectComparisonExpression()

        while (maybeGetCurrentToken() in equalityTokens) {
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
        var expression = expectPrefixExpression()

        while (maybeGetCurrentToken() in multiplicativeTokens) {
            expression = JavascriptExpression.BinaryOperation(
                operator = expectToken(),
                lhs = expression,
                rhs = expectPrefixExpression()
            )
        }

        return expression
    }

    private fun expectPrefixExpression(): JavascriptExpression {
        val prefixTokenStack = Stack<JavascriptTokenType>()

        while (maybeGetCurrentToken() in prefixTokens) {
            prefixTokenStack.push(expectToken())
        }

        var expression = expectPostfixIncrementExpression()

        while (prefixTokenStack.isNotEmpty()) {
            expression = JavascriptExpression.UnaryOperation(
                operator = prefixTokenStack.pop(),
                expression = expression,
                isPrefix = true
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
        var expression = if (getCurrentToken() is JavascriptTokenType.New) {
            expectNewExpression()
        } else {
            expectSimpleExpression()
        }

        var continueParsing = true

        loop@ while (continueParsing) {
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

    private fun expectNewExpression(): JavascriptExpression.NewCall {
        expectToken<JavascriptTokenType.New>()
        val identifier = expectToken<JavascriptTokenType.Identifier>()

        return if (maybeGetCurrentToken() is JavascriptTokenType.OpenParentheses) {
            JavascriptExpression.NewCall(
                function = expectFunctionCallOn(JavascriptExpression.Reference(identifier.name))
            )
        } else {
            JavascriptExpression.NewCall(
                function = JavascriptExpression.FunctionCall(
                    expression = JavascriptExpression.Reference(identifier.name),
                    parameters = emptyList()
                )
            )
        }
    }

    private fun expectFunctionCallOn(expression: JavascriptExpression): JavascriptExpression.FunctionCall {
        expectToken<JavascriptTokenType.OpenParentheses>()
        val arguments = mutableListOf<JavascriptExpression>()

        if (getCurrentToken() !is JavascriptTokenType.CloseParentheses) {
            arguments += expectSubExpression()

            while (getCurrentToken() !is JavascriptTokenType.CloseParentheses) {
                expectToken<JavascriptTokenType.Comma>()
                arguments += expectSubExpression()
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
        val index = expectSubExpression()
        expectToken<JavascriptTokenType.CloseBracket>()

        return JavascriptExpression.IndexAccess(
            indexExpression = index,
            expression = expression
        )
    }

    private fun expectDotAccessOn(expression: JavascriptExpression): JavascriptExpression.DotAccess {
        expectToken<JavascriptTokenType.Dot>()

        return JavascriptExpression.DotAccess(
            expression = expression,
            propertyName = expectToken<JavascriptTokenType.Identifier>().name
        )
    }

    private fun expectSimpleExpression(): JavascriptExpression {
        return when (val currentToken = getCurrentToken()) {
            is JavascriptTokenType.OpenParentheses -> expectGroupExpression()
            is JavascriptTokenType.OpenCurlyBracket -> expectObjectLiteral()
            is JavascriptTokenType.OpenBracket -> expectArrayLiteral()
            is JavascriptTokenType.Function -> expectAnonymousFunctionExpression()
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
            is JavascriptTokenType.RegularExpression -> {
                advanceCursor()
                JavascriptExpression.Literal(
                    value = JavascriptValue.Object(
                        JavascriptRegex(
                            currentToken.regex,
                            currentToken.flags
                        )
                    )
                )
            }
            else -> throwUnexpectedTokenFound()
        }
    }

    private fun expectArrayLiteral(): JavascriptExpression {
        val values = mutableListOf<JavascriptExpression>()

        expectToken<JavascriptTokenType.OpenBracket>()

        if (getCurrentToken() !is JavascriptTokenType.CloseBracket) {
            values += expectSubExpression()

            loop@ while (getCurrentToken() !is JavascriptTokenType.CloseBracket) {
                expectToken<JavascriptTokenType.Comma>()

                when (getCurrentToken()) {
                    is JavascriptTokenType.CloseBracket -> break@loop
                    else -> values += expectSubExpression()
                }
            }
        }

        expectToken<JavascriptTokenType.CloseBracket>()

        return JavascriptExpression.ArrayLiteral(values)
    }

    private fun expectObjectLiteral(): JavascriptExpression {
        val fields = mutableListOf<JavascriptExpression.ObjectLiteral.Field>()
        expectToken<JavascriptTokenType.OpenCurlyBracket>()

        if (getCurrentToken() !is JavascriptTokenType.CloseCurlyBracket) {
            fields += expectObjectField()

            loop@ while (getCurrentToken() !is JavascriptTokenType.CloseCurlyBracket) {
                expectToken<JavascriptTokenType.Comma>()

                when (getCurrentToken()) {
                    is JavascriptTokenType.CloseCurlyBracket -> break@loop
                    else -> fields += expectObjectField()
                }
            }
        }

        expectToken<JavascriptTokenType.CloseCurlyBracket>()

        return JavascriptExpression.ObjectLiteral(fields)
    }

    private fun expectObjectField(): JavascriptExpression.ObjectLiteral.Field {
        return JavascriptExpression.ObjectLiteral.Field(
            name = expectObjectKey().also { expectToken<JavascriptTokenType.Colon>() },
            rhs = expectSubExpression()
        )
    }

    private fun expectObjectKey(): String {
        return when (val currentToken = getCurrentToken()) {
            is JavascriptTokenType.Number -> {
                advanceCursor()
                JavascriptValue.Number(currentToken.value).toString()
            }
            is JavascriptTokenType.String -> {
                advanceCursor()
                JavascriptValue.String(currentToken.value).toString()
            }
            is JavascriptTokenType.Boolean -> {
                advanceCursor()
                JavascriptValue.Boolean(currentToken.value).toString()
            }
            is JavascriptTokenType.Undefined -> {
                advanceCursor()
                JavascriptValue.Undefined.toString()
            }
            is JavascriptTokenType.Identifier -> {
                advanceCursor()
                currentToken.name
            }
            else -> throwUnexpectedTokenFound()
        }
    }

    private fun expectGroupExpression(): JavascriptExpression {
        expectToken<JavascriptTokenType.OpenParentheses>()
        val expression = expectExpression()
        expectToken<JavascriptTokenType.CloseParentheses>()
        return expression
    }

    private fun expectAnonymousFunctionExpression(): JavascriptExpression {
        expectToken<JavascriptTokenType.Function>()
        val name = tryGetToken<JavascriptTokenType.Identifier>()?.name
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

        return JavascriptExpression.AnonymousFunction(
            name = name,
            parameterNames = parameterNames.map { it.name },
            body = expectBlock()
        )
    }

    private fun advanceCursor() {
        cursor += 1
    }

    private inline fun <reified T : JavascriptTokenType> expectToken(): T {
        if (getCurrentToken() !is T) {
            throwUnexpectedTokenFound(T::class.java.simpleName)
        }

        return (getCurrentToken() as T).also {
            advanceCursor()
        }
    }

    private fun throwUnexpectedTokenFound(expectedToken: String? = null): Nothing {
        val sourceInfo = tokens[cursor].sourceInfo
        val topLine =
            "($sourceFileName:${sourceInfo.line + 1}) column:${sourceInfo.column + 1} Uncaught SyntaxError: Unexpected token"
        val errorLines = sourceLines.subList(max(0, sourceInfo.line - 3), sourceInfo.line + 1)

        val message = "$topLine${expectedToken?.let { ", expected: $it" } ?: ""}\n${errorLines.joinToString("\n")}\n${" ".repeat(sourceInfo.column)}^"

        throw UnexpectedTokenException(message)
    }

    private inline fun <reified T : JavascriptTokenType> tryGetToken(): T? {
        if (getCurrentToken() !is T) {
            return null
        }

        return (getCurrentToken() as? T).also {
            advanceCursor()
        }
    }

    private inline fun maybeConsumeLineTerminator() {
        if (isAtEnd()) {
            return
        }
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

    private fun maybeGetNextToken(): JavascriptTokenType? {
        if (cursor + 1 >= tokens.size) {
            return null
        }
        return tokens[cursor + 1].type
    }

    private fun isAtEnd(): Boolean {
        return cursor >= tokens.size
    }

    private fun checkpoint() {
        savedCursor = cursor
    }

    private fun revertToCheckpoint() {
        cursor = savedCursor
    }

    private fun JavascriptExpression.convertToRightToLeftAssociativity(): JavascriptExpression {
        if (this !is JavascriptExpression.BinaryOperation) {
            return this
        }

        var currentExpression: JavascriptExpression.BinaryOperation = this

        while (
            currentExpression.operator in rightToLeftAssociativeOperators &&
            currentExpression.lhs is JavascriptExpression.BinaryOperation &&
            currentExpression.operator == (currentExpression.lhs as JavascriptExpression.BinaryOperation).operator
        ) {
            val lhsBinaryExpression = currentExpression.lhs as JavascriptExpression.BinaryOperation

            currentExpression = JavascriptExpression.BinaryOperation(
                operator = lhsBinaryExpression.operator,
                lhs = lhsBinaryExpression.lhs,
                rhs = JavascriptExpression.BinaryOperation(
                    operator = currentExpression.operator,
                    lhs = lhsBinaryExpression.rhs,
                    rhs = currentExpression.rhs
                )
            )
        }

        return currentExpression
    }

    class UnexpectedTokenException(message: String) : Exception(message)
    class UnexpectedEndOfFileException : Exception("Uncaught SyntaxError: Unexpected eof.")
}
