package ca.antonious.browser.libraries.javascript.parser

import ca.antonious.browser.libraries.javascript.ast.AssignmentStatement
import ca.antonious.browser.libraries.javascript.ast.AssignmentTarget
import ca.antonious.browser.libraries.javascript.ast.ClassBody
import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptProgram
import ca.antonious.browser.libraries.javascript.ast.JavascriptStatement
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.lexer.JavascriptToken
import ca.antonious.browser.libraries.javascript.lexer.JavascriptTokenType
import ca.antonious.browser.libraries.shared.parsing.SourceInfo
import java.util.Stack
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
            JavascriptTokenType.Operator.Divide,
            JavascriptTokenType.Operator.Mod
        )

        private val comparisonTokens = setOf(
            JavascriptTokenType.Operator.LessThan,
            JavascriptTokenType.Operator.LessThanOrEqual,
            JavascriptTokenType.Operator.GreaterThan,
            JavascriptTokenType.Operator.GreaterThanOrEqual,
            JavascriptTokenType.In,
            JavascriptTokenType.InstanceOf
        )

        private val bitShiftTokens = setOf(
            JavascriptTokenType.Operator.LeftShift,
            JavascriptTokenType.Operator.RightShift
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
            JavascriptTokenType.Void,
            JavascriptTokenType.Delete
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
            is JavascriptTokenType.Class -> expectClassDeclaration()
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
            is JavascriptTokenType.Break -> expectBreakStatement()
            is JavascriptTokenType.Continue -> expectContinueStatement()
            else -> {
                val expression = expectExpression()
                JavascriptStatement.Expression(sourceInfo = expression.sourceInfo, expression = expression)
            }
        }
    }

    private fun expectBreakStatement(): JavascriptStatement {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.Break>()

        val label = if (maybeGetCurrentToken() is JavascriptTokenType.Identifier) {
            expectToken<JavascriptTokenType.Identifier>().name
        } else {
            null
        }

        return JavascriptStatement.Break(sourceInfo = sourceInfo, label = label)
    }

    private fun expectContinueStatement(): JavascriptStatement {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.Continue>()

        val label = if (maybeGetCurrentToken() is JavascriptTokenType.Identifier) {
            expectToken<JavascriptTokenType.Identifier>().name
        } else {
            null
        }

        return JavascriptStatement.Continue(sourceInfo = sourceInfo, label = label)
    }

    private fun expectThrowStatement(): JavascriptStatement {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.Throw>()
        return JavascriptStatement.Throw(sourceInfo = sourceInfo, expression = expectExpression())
    }

    private fun expectTryStatement(): JavascriptStatement {
        var errorName: String? = null
        var catchBlock: JavascriptStatement.Block? = null
        var finallyBlock: JavascriptStatement.Block? = null

        val sourceInfo = expectSourceInfo<JavascriptTokenType.Try>()
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
            sourceInfo = sourceInfo,
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
            val (label, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType.Identifier>()
            expectToken<JavascriptTokenType.Colon>()
            return JavascriptStatement.LabeledStatement(sourceInfo = sourceInfo, label = label.name, statement = expectStatement())
        } else {
            val expression = expectExpression()
            JavascriptStatement.Expression(sourceInfo = expression.sourceInfo, expression = expression)
        }
    }

    private fun expectIfStatement(): JavascriptStatement.IfStatement {
        val conditions = mutableListOf<JavascriptStatement.IfStatement.ConditionAndStatement>()

        val sourceInfo = expectSourceInfo<JavascriptTokenType.If>()
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
                        condition = JavascriptExpression.Literal(SourceInfo(0, 0), JavascriptValue.Boolean(true)),
                        body = expectBlockOrStatement()
                    )
                }
            }
        }

        return JavascriptStatement.IfStatement(sourceInfo = sourceInfo, conditions = conditions)
    }

    private fun expectFunctionDeclaration(): JavascriptStatement.Function {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.Function>()
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
            sourceInfo = sourceInfo,
            name = functionName.name,
            parameterNames = parameterNames.map { it.name },
            body = expectBlock()
        )
    }

    private fun expectClassDeclaration(): JavascriptStatement.ClassDeclaration {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.Class>()
        val className = expectToken<JavascriptTokenType.Identifier>()

        return JavascriptStatement.ClassDeclaration(
            sourceInfo = sourceInfo,
            name = className.name,
            body = expectClassBody()
        )
    }

    private fun expectReturnStatement(): JavascriptStatement.Return {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.Return>()

        return when (maybeGetCurrentToken()) {
            is JavascriptTokenType.SemiColon -> {
                advanceCursor()
                JavascriptStatement.Return(sourceInfo = sourceInfo, expression = null)
            }
            is JavascriptTokenType.CloseCurlyBracket, null -> {
                JavascriptStatement.Return(sourceInfo = sourceInfo, expression = null)
            }
            else -> {
                JavascriptStatement.Return(sourceInfo = sourceInfo, expression = expectExpression())
            }
        }
    }

    private fun expectWhileLoop(): JavascriptStatement.WhileLoop {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.While>()

        expectToken<JavascriptTokenType.OpenParentheses>()
        val condition = expectExpression()
        expectToken<JavascriptTokenType.CloseParentheses>()

        maybeConsumeLineTerminator()

        return JavascriptStatement.WhileLoop(
            sourceInfo = sourceInfo,
            condition = condition,
            body = expectBlockOrStatement()
        )
    }

    private fun expectDoWhileLoop(): JavascriptStatement {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.Do>()
        val body = expectBlockOrStatement()
        expectToken<JavascriptTokenType.While>()
        expectToken<JavascriptTokenType.OpenParentheses>()
        val condition = expectExpression()
        expectToken<JavascriptTokenType.CloseParentheses>()

        return JavascriptStatement.DoWhileLoop(sourceInfo = sourceInfo, body = body, condition = condition)
    }

    private fun expectForLoop(): JavascriptStatement {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.For>()
        expectToken<JavascriptTokenType.OpenParentheses>()

        if (getCurrentToken() is JavascriptTokenType.SemiColon) {
            return expectCStyleForLoop(sourceInfo = sourceInfo, initializerStatement = null)
        }

        val savedCursorPosition = cursor
        val forStatement = expectStatement()
        val forStatementAsExpression = (forStatement as? JavascriptStatement.Expression)?.expression

        if (forStatementAsExpression is JavascriptExpression.BinaryOperation && forStatementAsExpression.operator is JavascriptTokenType.In) {
            expectToken<JavascriptTokenType.CloseParentheses>()
            return JavascriptStatement.ForEachLoop(
                sourceInfo = sourceInfo,
                initializerStatement = forStatementAsExpression.lhs,
                enumerableExpression = forStatementAsExpression.rhs,
                body = expectBlockOrStatementOrNothing()
            )
        }

        cursor = savedCursorPosition

        val initializerStatement = expectStatement()

        return when (getCurrentToken()) {
            is JavascriptTokenType.SemiColon -> expectCStyleForLoop(sourceInfo, initializerStatement)
            is JavascriptTokenType.In -> expectForEachLoop(sourceInfo, initializerStatement)
            else -> throwUnexpectedTokenFound()
        }
    }

    private fun expectCStyleForLoop(sourceInfo: SourceInfo, initializerStatement: JavascriptStatement?): JavascriptStatement {
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
            sourceInfo = sourceInfo,
            initializerStatement = initializerStatement,
            conditionExpression = conditionExpression,
            updaterExpression = updaterExpression,
            body = expectBlockOrStatementOrNothing()
        )
    }

    private fun expectForEachLoop(sourceInfo: SourceInfo, initializerStatement: JavascriptStatement): JavascriptStatement {
        expectToken<JavascriptTokenType.In>()
        val enumerableExpression = expectExpression()
        expectToken<JavascriptTokenType.CloseParentheses>()

        return JavascriptStatement.ForEachLoop(
            sourceInfo = sourceInfo,
            initializerStatement = initializerStatement,
            enumerableExpression = enumerableExpression,
            body = expectBlockOrStatementOrNothing()
        )
    }

    private fun expectBlockOrStatement(): JavascriptStatement {
        return when {
            getCurrentToken() is JavascriptTokenType.OpenCurlyBracket -> expectBlock()
            else -> expectStatement().also { maybeConsumeLineTerminator() }
        }
    }

    private fun expectBlockOrStatementOrNothing(): JavascriptStatement? {
        return when {
            getCurrentToken() is JavascriptTokenType.OpenCurlyBracket -> expectBlock()
            getCurrentToken() is JavascriptTokenType.SemiColon -> null
            else -> expectStatement().also { maybeConsumeLineTerminator() }
        }
    }

    private fun expectBlockOrExpression(): JavascriptStatement {
        return when {
            getCurrentToken() is JavascriptTokenType.OpenCurlyBracket -> expectBlock()
            else -> expectExpression()
        }
    }

    private fun expectBlock(): JavascriptStatement.Block {
        val statements = mutableListOf<JavascriptStatement>()

        val sourceInfo = expectSourceInfo<JavascriptTokenType.OpenCurlyBracket>()
        maybeConsumeLineTerminator()

        while (getCurrentToken() !is JavascriptTokenType.CloseCurlyBracket) {
            statements += expectStatement()
            maybeConsumeLineTerminator()
        }

        expectToken<JavascriptTokenType.CloseCurlyBracket>()

        return JavascriptStatement.Block(sourceInfo = sourceInfo, body = statements)
    }

    private fun expectVarStatement(): JavascriptStatement.VarAssignment {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.Var>()
        return JavascriptStatement.VarAssignment(sourceInfo = sourceInfo, assignments = expectAssignmentStatements())
    }

    private fun expectLetStatement(): JavascriptStatement.LetAssignment {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.Let>()
        return JavascriptStatement.LetAssignment(sourceInfo = sourceInfo, assignments = expectAssignmentStatements())
    }

    private fun expectConstStatement(): JavascriptStatement.ConstAssignment {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.Const>()
        return JavascriptStatement.ConstAssignment(sourceInfo = sourceInfo, assignments = expectAssignmentStatements())
    }

    private fun expectAssignmentStatements(): List<AssignmentStatement> {
        val statements = mutableListOf<AssignmentStatement>()

        loop@ while (true) {
            when (maybeGetCurrentToken()) {
                is JavascriptTokenType.Comma -> expectToken<JavascriptTokenType.Comma>()
                is JavascriptTokenType.Identifier,
                is JavascriptTokenType.OpenBracket,
                is JavascriptTokenType.OpenCurlyBracket -> {
                    statements += AssignmentStatement(
                        target = expectAssignmentTarget(),
                        expression = if (getCurrentToken() == JavascriptTokenType.Operator.Assignment) {
                            expectToken<JavascriptTokenType.Operator.Assignment>()
                            expectSubExpression()
                        } else {
                            null
                        }
                    )

                    if (maybeGetCurrentToken() !is JavascriptTokenType.Comma) {
                        break@loop
                    }
                }
                else ->  break@loop
            }
        }

        return statements
    }

    private fun expectExpression(): JavascriptExpression {
        return expectCommaExpression()
    }

    private fun expectSubExpression(): JavascriptExpression {
        return expectAssignmentExpression()
    }

    private fun expectCommaExpression(): JavascriptExpression {
        var expression = expectAssignmentExpression()

        while (maybeGetCurrentToken() is JavascriptTokenType.Comma) {
            val (token, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType>()
            expression = JavascriptExpression.BinaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
                lhs = expression,
                rhs = expectAssignmentExpression()
            )
        }

        return expression
    }

    private fun expectAssignmentExpression(): JavascriptExpression {
        var expression = expectTernaryExpression()

        while (maybeGetCurrentToken() in assignmentToken) {
            val (token, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType>()

            expression = JavascriptExpression.BinaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
                lhs = expression,
                rhs = expectTernaryExpression()
            )
        }

        return expression.convertToRightToLeftAssociativity()
    }

    private fun expectTernaryExpression(): JavascriptExpression {
        val expression = expectNullishCoalescingExpression()

        if (maybeGetCurrentToken() is JavascriptTokenType.QuestionMark) {
            val sourceInfo = expectSourceInfo<JavascriptTokenType.QuestionMark>()
            return JavascriptExpression.TernaryOperation(
                sourceInfo = sourceInfo,
                condition = expression,
                ifTruthy = expectTernaryExpression().also { expectToken<JavascriptTokenType.Colon>() },
                ifNot = expectTernaryExpression()
            )
        }

        return expression
    }

    private fun expectNullishCoalescingExpression(): JavascriptExpression {
        var expression = expectLogicalOrExpression()

        while (maybeGetCurrentToken() is JavascriptTokenType.Operator.QuestionQuestion) {
            val (token, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType>()

            expression = JavascriptExpression.BinaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
                lhs = expression,
                rhs = expectLogicalOrExpression()
            )
        }

        return expression
    }


    private fun expectLogicalOrExpression(): JavascriptExpression {
        var expression = expectLogicalAndExpression()

        while (maybeGetCurrentToken() is JavascriptTokenType.Operator.OrOr) {
            val (token, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType>()

            expression = JavascriptExpression.BinaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
                lhs = expression,
                rhs = expectLogicalAndExpression()
            )
        }

        return expression
    }

    private fun expectLogicalAndExpression(): JavascriptExpression {
        var expression = expectBitwiseOrExpression()

        while (maybeGetCurrentToken() is JavascriptTokenType.Operator.AndAnd) {
            val (token, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType>()

            expression = JavascriptExpression.BinaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
                lhs = expression,
                rhs = expectBitwiseOrExpression()
            )
        }

        return expression
    }

    private fun expectBitwiseOrExpression(): JavascriptExpression {
        var expression = expectXorExpression()

        while (maybeGetCurrentToken() is JavascriptTokenType.Operator.Or) {
            val (token, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType>()

            expression = JavascriptExpression.BinaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
                lhs = expression,
                rhs = expectXorExpression()
            )
        }

        return expression
    }

    private fun expectXorExpression(): JavascriptExpression {
        var expression = expectBitwiseAndExpression()

        while (maybeGetCurrentToken() is JavascriptTokenType.Operator.Xor) {
            val (token, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType>()

            expression = JavascriptExpression.BinaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
                lhs = expression,
                rhs = expectBitwiseAndExpression()
            )
        }

        return expression
    }

    private fun expectBitwiseAndExpression(): JavascriptExpression {
        var expression = expectEqualityExpression()

        while (maybeGetCurrentToken() is JavascriptTokenType.Operator.And) {
            val (token, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType>()

            expression = JavascriptExpression.BinaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
                lhs = expression,
                rhs = expectEqualityExpression()
            )
        }

        return expression
    }

    private fun expectEqualityExpression(): JavascriptExpression {
        var expression = expectComparisonExpression()

        while (maybeGetCurrentToken() in equalityTokens) {
            val (token, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType>()

            expression = JavascriptExpression.BinaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
                lhs = expression,
                rhs = expectComparisonExpression()
            )
        }

        return expression
    }

    private fun expectComparisonExpression(): JavascriptExpression {
        var expression = expectBitShiftExpression()

        while (maybeGetCurrentToken() in comparisonTokens) {
            val (token, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType>()

            expression = JavascriptExpression.BinaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
                lhs = expression,
                rhs = expectBitShiftExpression()
            )
        }

        return expression
    }

    private fun expectBitShiftExpression(): JavascriptExpression {
        var expression = expectAdditiveExpression()

        while (maybeGetCurrentToken() in bitShiftTokens) {
            val (token, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType>()

            expression = JavascriptExpression.BinaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
                lhs = expression,
                rhs = expectAdditiveExpression()
            )
        }

        return expression
    }

    private fun expectAdditiveExpression(): JavascriptExpression {
        var expression = expectMultiplicativeExpression()

        while (maybeGetCurrentToken() in additiveTokens) {
            val (token, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType>()

            expression = JavascriptExpression.BinaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
                lhs = expression,
                rhs = expectMultiplicativeExpression()
            )
        }

        return expression
    }

    private fun expectMultiplicativeExpression(): JavascriptExpression {
        var expression = expectPrefixExpression()

        while (maybeGetCurrentToken() in multiplicativeTokens) {
            val (token, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType>()

            expression = JavascriptExpression.BinaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
                lhs = expression,
                rhs = expectPrefixExpression()
            )
        }

        return expression
    }

    private fun expectPrefixExpression(): JavascriptExpression {
        val prefixTokenStack = Stack<Pair<JavascriptTokenType, SourceInfo>>()

        while (maybeGetCurrentToken() in prefixTokens) {
            prefixTokenStack.push(expectTokenAndSourceInfo())
        }

        var expression = expectPostfixIncrementExpression()

        while (prefixTokenStack.isNotEmpty()) {
            val (token, sourceInfo) = prefixTokenStack.pop()
            expression = JavascriptExpression.UnaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
                expression = expression,
                isPrefix = true
            )
        }

        return expression
    }

    private fun expectPostfixIncrementExpression(): JavascriptExpression {
        val expression = expectPostfixExpression()

        if (maybeGetCurrentToken() in incrementTokens) {
            val (token, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType>()

            return JavascriptExpression.UnaryOperation(
                sourceInfo = sourceInfo,
                operator = token,
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
        val newSourceInfo = expectSourceInfo<JavascriptTokenType.New>()
        val newTarget = expectPostfixExpression()

        return if (newTarget is JavascriptExpression.FunctionCall) {
            JavascriptExpression.NewCall(
                sourceInfo = newSourceInfo,
                function = newTarget
            )
        } else {
            JavascriptExpression.NewCall(
                sourceInfo = newSourceInfo,
                function = JavascriptExpression.FunctionCall(
                    sourceInfo = newSourceInfo,
                    expression = newTarget,
                    parameters = emptyList()
                )
            )
        }
    }

    private fun expectFunctionCallOn(expression: JavascriptExpression): JavascriptExpression.FunctionCall {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.OpenParentheses>()
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
            sourceInfo = sourceInfo,
            expression = expression,
            parameters = arguments
        )
    }

    private fun expectIndexAccessOn(expression: JavascriptExpression): JavascriptExpression.IndexAccess {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.OpenBracket>()
        val index = expectSubExpression()
        expectToken<JavascriptTokenType.CloseBracket>()

        return JavascriptExpression.IndexAccess(
            sourceInfo = sourceInfo,
            indexExpression = index,
            expression = expression
        )
    }

    private fun expectDotAccessOn(expression: JavascriptExpression): JavascriptExpression.DotAccess {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.Dot>()

        return JavascriptExpression.DotAccess(
            sourceInfo = sourceInfo,
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
            is JavascriptTokenType.Class -> expectClassExpression()
            is JavascriptTokenType.Number -> {
                val sourceInfo = expectSourceInfo<JavascriptTokenType>()
                JavascriptExpression.Literal(sourceInfo = sourceInfo, value = JavascriptValue.Number(currentToken.value))
            }
            is JavascriptTokenType.String -> {
                val sourceInfo = expectSourceInfo<JavascriptTokenType>()
                JavascriptExpression.Literal(sourceInfo = sourceInfo, value = JavascriptValue.String(currentToken.value))
            }
            is JavascriptTokenType.Boolean -> {
                val sourceInfo = expectSourceInfo<JavascriptTokenType>()
                JavascriptExpression.Literal(sourceInfo = sourceInfo, value = JavascriptValue.Boolean(currentToken.value))
            }
            is JavascriptTokenType.Undefined -> {
                val sourceInfo = expectSourceInfo<JavascriptTokenType>()
                JavascriptExpression.Literal(sourceInfo = sourceInfo, value = JavascriptValue.Undefined)
            }
            is JavascriptTokenType.Null -> {
                val sourceInfo = expectSourceInfo<JavascriptTokenType>()
                JavascriptExpression.Literal(sourceInfo = sourceInfo, value = JavascriptValue.Null)
            }
            is JavascriptTokenType.Identifier -> {
                val sourceInfo = expectSourceInfo<JavascriptTokenType>()
                JavascriptExpression.Reference(sourceInfo = sourceInfo, name = currentToken.name)
            }
            is JavascriptTokenType.RegularExpression -> {
                val sourceInfo = expectSourceInfo<JavascriptTokenType>()
                JavascriptExpression.RegexLiteral(
                    sourceInfo = sourceInfo,
                    pattern = currentToken.regex,
                    flags = currentToken.flags
                )
            }
            else -> throwUnexpectedTokenFound()
        }
    }

    private fun expectArrayLiteral(): JavascriptExpression {
        val values = mutableListOf<JavascriptExpression>()

        val sourceInfo = expectSourceInfo<JavascriptTokenType.OpenBracket>()

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

        return JavascriptExpression.ArrayLiteral(sourceInfo, values)
    }

    private fun expectObjectLiteral(): JavascriptExpression {
        val fields = mutableListOf<JavascriptExpression.ObjectLiteral.Field>()
        val sourceInfo = expectSourceInfo<JavascriptTokenType.OpenCurlyBracket>()

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

        return JavascriptExpression.ObjectLiteral(sourceInfo, fields)
    }

    private fun expectObjectField(): JavascriptExpression.ObjectLiteral.Field {
        if (getCurrentToken() is JavascriptTokenType.TripleDot) {
            expectToken<JavascriptTokenType.TripleDot>()
            return JavascriptExpression.ObjectLiteral.Field.Spread(
                expression = expectSubExpression()
            )
        }

        val objectKey = expectObjectKey()

        return if (getCurrentToken() is JavascriptTokenType.Colon) {
            expectToken<JavascriptTokenType.Colon>()
            JavascriptExpression.ObjectLiteral.Field.Value(
                name = objectKey,
                rhs = expectSubExpression()
            )
        } else {
            when (objectKey) {
                "get" -> {
                    val (key, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType.Identifier>()
                    JavascriptExpression.ObjectLiteral.Field.Getter(
                        name = key.name,
                        rhs = JavascriptExpression.AnonymousFunction(
                            sourceInfo = sourceInfo,
                            name = "get ${key.name}",
                            parameterNames = expectFunctionParameters().map { it.name },
                            body = expectBlock()
                        )
                    )
                }
                "set" -> {
                    val (key, sourceInfo) = expectTokenAndSourceInfo<JavascriptTokenType.Identifier>()
                    JavascriptExpression.ObjectLiteral.Field.Setter(
                        name = key.name,
                        rhs = JavascriptExpression.AnonymousFunction(
                            sourceInfo = sourceInfo,
                            name = "set ${key.name}",
                            parameterNames = expectFunctionParameters().map { it.name },
                            body = expectBlock()
                        )
                    )
                }
                else -> throwUnexpectedTokenFound()
            }
        }
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
        val savedCursorPosition = cursor
        expectToken<JavascriptTokenType.OpenParentheses>()

        if (getCurrentToken() is JavascriptTokenType.CloseParentheses) {
            cursor = savedCursorPosition
            return expectArrowFunction()
        }

        val expression = expectExpression()
        expectToken<JavascriptTokenType.CloseParentheses>()

        return if (maybeGetCurrentToken() is JavascriptTokenType.Arrow) {
            cursor = savedCursorPosition
            expectArrowFunction()
        } else {
            expression
        }
    }

    private fun expectArrowFunction(): JavascriptExpression {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.OpenParentheses>()

        val parameterNames = mutableListOf<JavascriptTokenType.Identifier>()

        if (getCurrentToken() !is JavascriptTokenType.CloseParentheses) {
            parameterNames += expectToken<JavascriptTokenType.Identifier>()

            while (getCurrentToken() !is JavascriptTokenType.CloseParentheses) {
                expectToken<JavascriptTokenType.Comma>()
                parameterNames += expectToken<JavascriptTokenType.Identifier>()
            }
        }

        expectToken<JavascriptTokenType.CloseParentheses>()
        expectToken<JavascriptTokenType.Arrow>()

        return JavascriptExpression.ArrowFunction(
            sourceInfo = sourceInfo,
            parameterNames = parameterNames.map { it.name },
            body = expectBlockOrExpression()
        )
    }

    private fun expectAnonymousFunctionExpression(): JavascriptExpression {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.Function>()
        val name = tryGetToken<JavascriptTokenType.Identifier>()?.name

        return JavascriptExpression.AnonymousFunction(
            sourceInfo = sourceInfo,
            name = name,
            parameterNames = expectFunctionParameters().map { it.name },
            body = expectBlock()
        )
    }

    private fun expectFunctionParameters(): List<JavascriptTokenType.Identifier> {
        val parameterNames = mutableListOf<JavascriptTokenType.Identifier>()

        expectToken<JavascriptTokenType.OpenParentheses>()

        if (getCurrentToken() !is JavascriptTokenType.CloseParentheses) {
            parameterNames += expectToken<JavascriptTokenType.Identifier>()

            while (getCurrentToken() !is JavascriptTokenType.CloseParentheses) {
                expectToken<JavascriptTokenType.Comma>()
                parameterNames += expectToken<JavascriptTokenType.Identifier>()
            }
        }

        expectToken<JavascriptTokenType.CloseParentheses>()

        return parameterNames
    }

    private fun expectAssignmentTarget(): AssignmentTarget {
        return when (getCurrentToken()) {
            is JavascriptTokenType.OpenCurlyBracket -> expectObjectDestructionTarget()
            is JavascriptTokenType.OpenBracket -> expectArrayDestructionTarget()
            is JavascriptTokenType.Identifier -> expectSimpleTarget()
            else -> throwUnexpectedTokenFound()
        }
    }

    private fun expectObjectDestructionTarget(): AssignmentTarget.ObjectDestructure {
        val targets = mutableListOf<AssignmentTarget.ObjectDestructure.DestructureTarget>()
        expectToken<JavascriptTokenType.OpenCurlyBracket>()

        while (getCurrentToken() != JavascriptTokenType.CloseCurlyBracket) {
            if (getCurrentToken() is JavascriptTokenType.TripleDot) {
                expectToken<JavascriptTokenType.TripleDot>()
                val restName = expectToken<JavascriptTokenType.Identifier>()
                targets += AssignmentTarget.ObjectDestructure.DestructureTarget.Rest(name = restName.name)

                if (getCurrentToken() !is JavascriptTokenType.CloseCurlyBracket) {
                    throwSyntaxError("Rest element must be last element")
                }

                break
            }

            val identifier = expectToken<JavascriptTokenType.Identifier>()

            val assignmentTarget = if (getCurrentToken() is JavascriptTokenType.Colon) {
                expectToken<JavascriptTokenType.Colon>()

                when (getCurrentToken()) {
                    is JavascriptTokenType.Identifier,
                    is JavascriptTokenType.OpenCurlyBracket,
                    is JavascriptTokenType.OpenBracket -> {
                        expectAssignmentTarget()
                    }
                    else -> null
                }
            } else {
                null
            }

            val defaultExpression = if (getCurrentToken() is JavascriptTokenType.Operator.Assignment) {
                expectToken<JavascriptTokenType.Operator.Assignment>()
                expectSubExpression()
            } else {
                null
            }

            targets += AssignmentTarget.ObjectDestructure.DestructureTarget.Single(
                propertyName = identifier.name,
                assignmentTarget = assignmentTarget,
                default = defaultExpression
            )

            if (getCurrentToken() is JavascriptTokenType.Comma) {
                expectToken<JavascriptTokenType.Comma>()
            } else {
                break
            }
        }

        expectToken<JavascriptTokenType.CloseCurlyBracket>()

        return AssignmentTarget.ObjectDestructure(targets)
    }

    private fun expectArrayDestructionTarget(): AssignmentTarget.ArrayDestructure {
        val targets = mutableListOf<AssignmentTarget.ArrayDestructure.DestructureTarget>()
        expectToken<JavascriptTokenType.OpenBracket>()

        while (getCurrentToken() != JavascriptTokenType.CloseBracket) {
            when (getCurrentToken()) {
                is JavascriptTokenType.Comma -> {
                    expectToken<JavascriptTokenType.Comma>()
                    targets += AssignmentTarget.ArrayDestructure.DestructureTarget.Empty
                }
                is JavascriptTokenType.TripleDot -> {
                    expectToken<JavascriptTokenType.TripleDot>()
                    val restName = expectToken<JavascriptTokenType.Identifier>()
                    targets += AssignmentTarget.ArrayDestructure.DestructureTarget.Rest(name = restName.name)

                    if (getCurrentToken() !is JavascriptTokenType.CloseBracket) {
                        throwSyntaxError("Rest element must be last element")
                    }
                    break
                }
                else -> {
                    val assignmentTarget = expectAssignmentTarget()

                    val defaultValueExpression = if (getCurrentToken() is JavascriptTokenType.Operator.Assignment) {
                        expectToken<JavascriptTokenType.Operator.Assignment>()
                        expectSubExpression()
                    } else {
                        null
                    }

                    targets += AssignmentTarget.ArrayDestructure.DestructureTarget.Single(
                        assignmentTarget = assignmentTarget,
                        default = defaultValueExpression
                    )
                }
            }

            if (getCurrentToken() is JavascriptTokenType.Comma) {
                expectToken<JavascriptTokenType.Comma>()
            }
        }

        expectToken<JavascriptTokenType.CloseBracket>()

        return AssignmentTarget.ArrayDestructure(targets)
    }

    private fun expectClassExpression(): JavascriptExpression.Class {
        val sourceInfo = expectSourceInfo<JavascriptTokenType.Class>()

        return JavascriptExpression.Class(
            sourceInfo = sourceInfo,
            name = if (getCurrentToken() is JavascriptTokenType.OpenCurlyBracket) {
                null
            } else {
                expectToken<JavascriptTokenType.Identifier>().name
            },
            body = expectClassBody()
        )
    }

    private fun expectClassBody(): ClassBody {
        val extendsExpression = if (getCurrentToken() is JavascriptTokenType.Extends) {
            expectToken<JavascriptTokenType.Extends>()
            expectExpression()
        } else {
            null
        }

        val classStatements = mutableListOf<ClassBody.Statement>()
        expectToken<JavascriptTokenType.OpenCurlyBracket>()

        var constructor: ClassBody.Constructor? = null

        while (getCurrentToken() !is JavascriptTokenType.CloseCurlyBracket) {
            when (getCurrentToken()) {
                is JavascriptTokenType.Constructor -> {
                    if (constructor != null) {
                        throwSyntaxError("A class may only have one constructor")
                    }
                    constructor = expectClassConstructor()
                }
                is JavascriptTokenType.Identifier -> classStatements += expectClassMethodOrMember(isStatic = false)
                is JavascriptTokenType.Static -> {
                    expectToken<JavascriptTokenType.Static>()
                    classStatements += when (getCurrentToken()) {
                        is JavascriptTokenType.Identifier -> expectClassMethodOrMember(isStatic = true)
                        else -> throwUnexpectedTokenFound()
                    }
                }
                is JavascriptTokenType.SemiColon -> expectToken<JavascriptTokenType.SemiColon>()
                else -> throwUnexpectedTokenFound()
            }
        }

        expectToken<JavascriptTokenType.CloseCurlyBracket>()

        return ClassBody(
            constructor = constructor,
            extends = extendsExpression,
            statements = classStatements
        )
    }

    private fun expectClassConstructor(): ClassBody.Constructor {
        expectToken<JavascriptTokenType.Constructor>()

        return ClassBody.Constructor(
            parameterNames = expectFunctionParameters().map { it.name },
            body = expectBlock()
        )
    }

    private fun expectClassMethodOrMember(isStatic: Boolean): ClassBody.Statement {
        return when (maybeGetNextToken()) {
            is JavascriptTokenType.Operator.Assignment -> expectClassMember(isStatic = isStatic)
            is JavascriptTokenType.OpenParentheses -> expectClassMethod(isStatic = isStatic)
            else -> throwUnexpectedTokenFound()
        }
    }

    private fun expectClassMember(isStatic: Boolean): ClassBody.Statement.Member {
        return ClassBody.Statement.Member(
            isStatic = isStatic,
            name = expectToken<JavascriptTokenType.Identifier>().name.also {
                expectToken<JavascriptTokenType.Operator.Assignment>()
            },
            expression = expectExpression()
        )
    }

    private fun expectClassMethod(isStatic: Boolean): ClassBody.Statement.Method {
        return ClassBody.Statement.Method(
            isStatic = isStatic,
            name = expectToken<JavascriptTokenType.Identifier>().name,
            parameterNames = expectFunctionParameters().map { it.name },
            body = expectBlock()
        )
    }

    private fun expectSimpleTarget(): AssignmentTarget.Simple {
        return AssignmentTarget.Simple(name = expectToken<JavascriptTokenType.Identifier>().name)
    }

    private fun advanceCursor() {
        cursor += 1
    }

    private inline fun <reified T : JavascriptTokenType> expectTokenAndSourceInfo(): Pair<T, SourceInfo> {
        if (getCurrentToken() !is T) {
            throwUnexpectedTokenFound(T::class.java.simpleName)
        }

        val token = tokens[cursor]
        return (token.type as T to token.sourceInfo).also {
            advanceCursor()
        }
    }

    private inline fun <reified T : JavascriptTokenType> expectToken(): T {
        return expectTokenAndSourceInfo<T>().first
    }

    private inline fun <reified T : JavascriptTokenType> expectSourceInfo(): SourceInfo {
        return expectTokenAndSourceInfo<T>().second
    }

    private fun throwUnexpectedTokenFound(expectedToken: String? = null): Nothing {
        throwSyntaxError("Unexpected token${expectedToken?.let { ", expected: $it" } ?: ""}")
    }

    private fun throwSyntaxError(message: String): Nothing {
        val sourceInfo = tokens[cursor].sourceInfo
        val topLine =
            "(${sourceInfo.filename}:${sourceInfo.line + 1}) column:${sourceInfo.column + 1} Uncaught SyntaxError: $message"
        val errorLines = sourceLines.subList(max(0, sourceInfo.line - 3), sourceInfo.line + 1)

        val message = "$topLine\n${errorLines.joinToString("\n")}\n${" ".repeat(sourceInfo.column)}^"

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
                sourceInfo = lhsBinaryExpression.sourceInfo,
                operator = lhsBinaryExpression.operator,
                lhs = lhsBinaryExpression.lhs,
                rhs = JavascriptExpression.BinaryOperation(
                    sourceInfo = currentExpression.sourceInfo,
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
