package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptExpression
import ca.antonious.browser.libraries.javascript.ast.JavascriptProgram
import ca.antonious.browser.libraries.javascript.ast.JavascriptStatement
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.builtins.`object`.ObjectConstructor
import ca.antonious.browser.libraries.javascript.interpreter.builtins.`object`.ObjectPrototype
import ca.antonious.browser.libraries.javascript.interpreter.builtins.array.ArrayConstructor
import ca.antonious.browser.libraries.javascript.interpreter.builtins.array.ArrayPrototype
import ca.antonious.browser.libraries.javascript.interpreter.builtins.array.ArrayObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.date.DateConstructor
import ca.antonious.browser.libraries.javascript.interpreter.builtins.date.DatePrototype
import ca.antonious.browser.libraries.javascript.interpreter.builtins.error.ErrorConstructor
import ca.antonious.browser.libraries.javascript.interpreter.builtins.error.ErrorPrototype
import ca.antonious.browser.libraries.javascript.interpreter.builtins.error.TypeErrorConstructor
import ca.antonious.browser.libraries.javascript.interpreter.builtins.error.TypeErrorPrototype
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.FunctionObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.FunctionPrototype
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.JavascriptFunction
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeExecutionContext
import ca.antonious.browser.libraries.javascript.interpreter.builtins.number.NumberConstructor
import ca.antonious.browser.libraries.javascript.interpreter.builtins.number.NumberObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.number.NumberPrototype
import ca.antonious.browser.libraries.javascript.interpreter.builtins.regex.RegExpConstructor
import ca.antonious.browser.libraries.javascript.interpreter.builtins.regex.RegExpObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.regex.RegExpPrototype
import ca.antonious.browser.libraries.javascript.interpreter.builtins.string.StringConstructor
import ca.antonious.browser.libraries.javascript.interpreter.builtins.string.StringObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.string.StringPrototype
import ca.antonious.browser.libraries.javascript.interpreter.debugger.server.JavascriptDebuggerServer
import ca.antonious.browser.libraries.javascript.lexer.JavascriptLexer
import ca.antonious.browser.libraries.javascript.lexer.JavascriptTokenType
import ca.antonious.browser.libraries.javascript.lexer.SourceInfo
import ca.antonious.browser.libraries.javascript.parser.JavascriptParser
import java.io.File
import java.util.Stack
import kotlin.random.Random

class JavascriptInterpreter {

    val objectPrototype = ObjectPrototype(this)
    val stringPrototype = StringPrototype(this)
    val functionPrototype = FunctionPrototype(this)
    val datePrototype = DatePrototype(this)
    val numberPrototype = NumberPrototype(this)
    val regExpPrototype = RegExpPrototype(this)
    val arrayPrototype = ArrayPrototype(this)
    val errorPrototype = ErrorPrototype(this)
    val typeErrorPrototype = TypeErrorPrototype(this)

    val globalObject = JavascriptObject(this, objectPrototype).apply {
        setNonEnumerableProperty("global", JavascriptValue.Object(this))
        setNonEnumerableProperty("Object", JavascriptValue.Object(ObjectConstructor(this@JavascriptInterpreter)))
        setNonEnumerableProperty("String", JavascriptValue.Object(StringConstructor(this@JavascriptInterpreter)))
        setNonEnumerableProperty("Number", JavascriptValue.Object(NumberConstructor(this@JavascriptInterpreter)))
        setNonEnumerableProperty("RegExp", JavascriptValue.Object(RegExpConstructor(this@JavascriptInterpreter)))
        setNonEnumerableProperty("Array", JavascriptValue.Object(ArrayConstructor(this@JavascriptInterpreter)))
        setNonEnumerableProperty("Date", JavascriptValue.Object(DateConstructor(this@JavascriptInterpreter)))
        setNonEnumerableProperty("Error", JavascriptValue.Object(ErrorConstructor(this@JavascriptInterpreter)))
        setNonEnumerableProperty("TypeError", JavascriptValue.Object(TypeErrorConstructor(this@JavascriptInterpreter)))
    }

    var stack = Stack<JavascriptStackFrame>().apply {
        push(
            JavascriptStackFrame(
                name = "main",
                scope = JavascriptScope(
                    thisBinding = globalObject,
                    globalObject = globalObject,
                    parentScope = null
                ),
                sourceInfo = SourceInfo(0, 0)
            )
        )
    }

    private var controlFlowInterruption: ControlFlowInterruption? = null

    private val currentScope: JavascriptScope
        get() = stack.peek().scope

    private val debugger = JavascriptDebuggerServer(this)

    init {
        initializeGlobalObject()
        objectPrototype.initialize()
        numberPrototype.initialize()
        stringPrototype.initialize()
        regExpPrototype.initialize()
        functionPrototype.initialize()
        datePrototype.initialize()
        arrayPrototype.initialize()
        errorPrototype.initialize()
        typeErrorPrototype.initialize()
    }

    fun interpret(file: File): JavascriptValue {
        return interpret(file.readText(), file.name)
    }

    fun interpret(javascript: String, filename: String = "unknown"): JavascriptValue {
        val filename = filename.trimEnd('/').split("/").last()
        val tokens = JavascriptLexer(javascript, filename).lex()
        val program = JavascriptParser(tokens, javascript).parse()
        return interpret(program)
    }

    fun interpretWithExplicitStack(
        javascript: String,
        filename: String = "unknown",
        stack: Stack<JavascriptStackFrame>
    ): JavascriptValue {
        val currentStack = this.stack
        this.stack = stack

        return interpret(javascript, filename).also {
            this.stack = currentStack
        }
    }

    fun interpret(program: JavascriptProgram): JavascriptValue {
        val value = interpret(JavascriptStatement.Block(sourceInfo = SourceInfo(0, 0), program.body, createsScope = false))

        return if (hasControlFlowInterruptedDueTo<ControlFlowInterruption.Error>()) {
            val tab = " ".repeat(4)
            val error = consumeControlFlowInterrupt<ControlFlowInterruption.Error>()
            val errorPrimitive = interpretAsString(error.value)
            val errorMessage = "Uncaught ${errorPrimitive}\n$tab" + error.trace.joinToString("\n$tab") {
                "at ${it.name}(${it.sourceInfo.filename}:${it.sourceInfo.line + 1}:${it.sourceInfo.column + 1})"
            }

            println(errorMessage)
            debugger.pauseOnError(error)
            error.value
        } else {
            value
        }
    }

    private fun interpret(statement: JavascriptStatement): JavascriptValue {
        return interpretAsReference(statement).value
    }

    private fun interpretAsReference(statement: JavascriptStatement): JavascriptReference {
        stack.peek().sourceInfo = statement.sourceInfo

        if (statement !is JavascriptExpression) {
            debugger.updateSourceLocation(statement.sourceInfo)
            debugger.awaitForContinue()
        }

        when (statement) {
            is JavascriptStatement.Expression -> {
                return interpretAsReference(statement.expression)
            }
            is JavascriptStatement.LabeledStatement -> {
                return interpretAsReference(statement.statement)
            }
            is JavascriptStatement.Block -> {
                val hoistedStatements = mutableListOf<JavascriptStatement>()
                val normalStatements = mutableListOf<JavascriptStatement>()

                if (statement.createsScope) enterScope()

                for (child in statement.body) {
                    when (child) {
                        is JavascriptStatement.Function -> hoistedStatements += child
                        else -> normalStatements += child
                    }
                }
                var result: JavascriptReference = JavascriptReference.Undefined

                for (child in hoistedStatements) {
                    result = interpretAsReference(child)
                    if (hasControlFlowInterrupted()) {
                        if (statement.createsScope) exitScope()
                        return result
                    }
                }

                for (child in normalStatements) {
                    result = interpretAsReference(child)
                    if (hasControlFlowInterrupted()) {
                        if (statement.createsScope) exitScope()
                        return result
                    }
                }

                if (statement.createsScope) exitScope()

                return result
            }
            is JavascriptStatement.Function -> {
                val value = JavascriptValue.Object(
                    value = JavascriptFunction(
                        interpreter = this,
                        name = statement.name,
                        parameterNames = statement.parameterNames,
                        body = statement.body,
                        parentScope = currentScope
                    )
                )
                currentScope.setVariable(key = statement.name, value = value)
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.Return -> {
                val value = if (statement.expression != null) {
                    interpret(statement.expression)
                } else {
                    JavascriptValue.Undefined
                }

                if (hasControlFlowInterrupted()) {
                    return JavascriptReference.Undefined
                }

                interruptControlFlowWith(ControlFlowInterruption.Return(value))
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.Break -> {
                interruptControlFlowWith(ControlFlowInterruption.Break(statement.label))
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.Continue -> {
                interruptControlFlowWith(ControlFlowInterruption.Continue(statement.label))
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.Throw -> {
                throwError(interpret(statement.expression))
                return JavascriptReference.Undefined
            }
            is JavascriptExpression.FunctionCall -> {
                val (valueToCall, thisBinding) = when (statement.expression) {
                    is JavascriptExpression.DotAccess -> {
                        val objectToInvoke = interpretAsObject(statement.expression.expression)
                        if (hasControlFlowInterrupted()) {
                            return JavascriptReference.Undefined
                        }

                        objectToInvoke.getProperty(statement.expression.propertyName) to objectToInvoke
                    }
                    else -> {
                        val valueToCallAndThisBinding = interpret(statement.expression) to globalObject
                        if (hasControlFlowInterrupted()) {
                            return JavascriptReference.Undefined
                        }
                        valueToCallAndThisBinding
                    }
                }

                return when (val objectToCall = (valueToCall as? JavascriptValue.Object)?.value) {
                    is FunctionObject -> {
                        val arguments = statement.parameters.map {
                            interpret(it).also {
                                if (hasControlFlowInterrupted()) {
                                    return JavascriptReference.Undefined
                                }
                            }
                        }

                        val nativeExecutionContext = NativeExecutionContext(
                            callLocation = statement.sourceInfo,
                            arguments = arguments,
                            thisBinding = objectToCall.boundThis ?: thisBinding,
                            interpreter = this
                        )

                        val returnValue = objectToCall.call(nativeExecutionContext)

                        if (hasControlFlowInterrupted()) {
                            JavascriptValue.Undefined
                        } else {
                            returnValue
                        }
                    }
                    else -> {
                        val functionTargetDescription = describeFunctionTarget(statement.expression)
                        throwTypeError("$functionTargetDescription is not a function")
                        JavascriptValue.Undefined
                    }
                }.toReference()
            }
            is JavascriptStatement.IfStatement -> {
                val conditionToExecute = statement.conditions.firstOrNull {
                    interpretPrimitiveValueOf(it.condition).isTruthy
                }
                if (conditionToExecute != null) {
                    interpret(conditionToExecute.body)
                }
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.LetAssignment -> {
                for (assignment in statement.assignments) {
                    val value = if (assignment.expression == null) {
                        JavascriptValue.Undefined
                    } else {
                        interpret(assignment.expression)
                    }

                    currentScope.setVariable(assignment.name, value)
                }

                return JavascriptReference.Undefined
            }
            is JavascriptStatement.ConstAssignment -> {
                for (assignment in statement.assignments) {
                    val value = if (assignment.expression == null) {
                        JavascriptValue.Undefined
                    } else {
                        interpret(assignment.expression)
                    }

                    currentScope.setVariable(assignment.name, value)
                }
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.VarAssignment -> {
                for (assignment in statement.assignments) {
                    val value = if (assignment.expression == null) {
                        JavascriptValue.Undefined
                    } else {
                        interpret(assignment.expression)
                    }

                    currentScope.setVariable(assignment.name, value)
                }
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.WhileLoop -> {
                while (!hasControlFlowInterrupted() && interpretPrimitiveValueOf(statement.condition).isTruthy) {
                    statement.body?.let { interpret(it) }

                    when {
                        hasControlFlowInterruptedDueTo<ControlFlowInterruption.Break>() -> {
                            consumeControlFlowInterrupt<ControlFlowInterruption.Break>()
                            break
                        }
                        hasControlFlowInterruptedDueTo<ControlFlowInterruption.Continue>() -> {
                            consumeControlFlowInterrupt<ControlFlowInterruption.Continue>()
                            continue
                        }
                    }
                }
                return JavascriptReference.Undefined
            }
            is JavascriptStatement.DoWhileLoop -> {
                do {
                    interpret(statement.body)
                } while (!hasControlFlowInterrupted() && interpretPrimitiveValueOf(statement.condition).isTruthy)

                return JavascriptReference.Undefined
            }
            is JavascriptStatement.ForLoop -> {
                if (statement.initializerStatement != null) {
                    interpret(statement.initializerStatement)
                }

                while (!hasControlFlowInterrupted() && statement.conditionExpression?.let { interpretPrimitiveValueOf(it).isTruthy } != false) {
                    statement.body?.let { interpret(it) }
                    statement.updaterExpression?.let { interpret(it) }
                }

                return JavascriptReference.Undefined
            }
            is JavascriptStatement.ForEachLoop -> {
                val initializerReference = interpretAsReference(statement.initializerStatement)
                val enumerableObject = interpretAsObject(statement.enumerableExpression)

                for (propertyKey in enumerableObject.enumerableKeys) {
                    initializerReference.setter?.invoke(JavascriptValue.String(propertyKey))
                        ?: error("Uncaught SyntaxError: Invalid left-hand side in assignment")
                    statement.body?.let { interpret(it) }
                    if (hasControlFlowInterrupted()) {
                        break
                    }
                }

                return JavascriptReference.Undefined
            }
            is JavascriptStatement.TryStatement -> {
                interpret(statement.tryBlock)

                if (hasControlFlowInterruptedDueTo<ControlFlowInterruption.Error>() && statement.catchBlock != null) {
                    val error = consumeControlFlowInterrupt<ControlFlowInterruption.Error>()

                    val scopeParameterNames = if (statement.errorName == null) {
                        emptyList()
                    } else {
                        listOf(statement.errorName)
                    }

                    val scopeParameters = if (statement.errorName == null) {
                        emptyList()
                    } else {
                        listOf(JavascriptExpression.Literal(SourceInfo(0, 0), error.value))
                    }

                    enterScope(scopeParameterNames, scopeParameters)
                    interpret(statement.catchBlock.copy(createsScope = false))
                    exitScope()
                }

                var catchControlFlowInterruption: ControlFlowInterruption? = null

                if (statement.finallyBlock != null) {
                    catchControlFlowInterruption = maybeConsumeControlFlowInterrupt()
                    interpret(statement.finallyBlock)
                }

                if (catchControlFlowInterruption != null && !hasControlFlowInterrupted()) {
                    interruptControlFlowWith(catchControlFlowInterruption)
                }

                return JavascriptReference.Undefined
            }
            is JavascriptExpression.TernaryOperation -> {
                return if (interpretPrimitiveValueOf(statement.condition).isTruthy) {
                    interpret(statement.ifTruthy)
                } else {
                    interpret(statement.ifNot)
                }.toReference()
            }
            is JavascriptExpression.BinaryOperation -> {
                return when (statement.operator) {
                    is JavascriptTokenType.Operator.OrOr -> {
                        val lhsValue = interpret(statement.lhs)
                        if (hasControlFlowInterrupted()) return JavascriptReference.Undefined

                        if (lhsValue.isTruthy) {
                            lhsValue.toReference()
                        } else {
                            val rhsValue = interpret(statement.rhs).toReference()
                            if (hasControlFlowInterrupted()) {
                                JavascriptReference.Undefined
                            } else {
                                rhsValue
                            }
                        }
                    }
                    is JavascriptTokenType.Operator.AndAnd -> {
                        val lhsValue = interpret(statement.lhs)
                        if (hasControlFlowInterrupted()) return JavascriptReference.Undefined

                        if (lhsValue.isTruthy) {
                            val rhsValue = interpret(statement.rhs).toReference()
                            if (hasControlFlowInterrupted()) {
                                JavascriptReference.Undefined
                            } else {
                                rhsValue
                            }
                        } else {
                            lhsValue.toReference()
                        }
                    }
                    is JavascriptTokenType.Operator.StrictEquals -> {
                        val lhsValue = interpret(statement.lhs)
                        if (hasControlFlowInterrupted()) return JavascriptReference.Undefined
                        val rhsValue = interpret(statement.rhs)
                        if (hasControlFlowInterrupted()) return JavascriptReference.Undefined

                        JavascriptValue.Boolean(lhsValue == rhsValue).toReference()
                    }
                    is JavascriptTokenType.Operator.StrictNotEquals -> {
                        val lhsValue = interpret(statement.lhs)
                        if (hasControlFlowInterrupted()) return JavascriptReference.Undefined
                        val rhsValue = interpret(statement.rhs)
                        if (hasControlFlowInterrupted()) return JavascriptReference.Undefined

                        JavascriptValue.Boolean(lhsValue != rhsValue).toReference()
                    }
                    is JavascriptTokenType.Operator.Assignment -> {
                        val valueToAssign = interpret(statement.rhs)
                        if (hasControlFlowInterrupted()) return JavascriptReference.Undefined

                        interpretAsReference(statement.lhs).setter?.invoke(valueToAssign)
                            ?: error("Uncaught SyntaxError: Invalid left-hand side in assignment")

                        if (hasControlFlowInterrupted()) return JavascriptReference.Undefined

                        valueToAssign.toReference()
                    }
                    is JavascriptTokenType.Operator.PlusAssign -> interpretOperatorAssignAsReference(
                        JavascriptTokenType.Operator.Plus,
                        statement
                    )
                    is JavascriptTokenType.Operator.MinusAssign -> interpretOperatorAssignAsReference(
                        JavascriptTokenType.Operator.Minus,
                        statement
                    )
                    is JavascriptTokenType.Operator.MultiplyAssign -> interpretOperatorAssignAsReference(
                        JavascriptTokenType.Operator.Multiply,
                        statement
                    )
                    is JavascriptTokenType.Operator.DivideAssign -> interpretOperatorAssignAsReference(
                        JavascriptTokenType.Operator.Divide,
                        statement
                    )
                    is JavascriptTokenType.Operator.XorAssign -> interpretOperatorAssignAsReference(
                        JavascriptTokenType.Operator.Xor,
                        statement
                    )
                    is JavascriptTokenType.Operator.ModAssign -> interpretOperatorAssignAsReference(
                        JavascriptTokenType.Operator.Mod,
                        statement
                    )
                    is JavascriptTokenType.Comma -> {
                        interpret(statement.lhs)
                        if (hasControlFlowInterrupted()) return JavascriptReference.Undefined
                        interpretAsReference(statement.rhs)
                    }
                    is JavascriptTokenType.In -> {
                        val lhsValue = interpret(statement.lhs)
                        return JavascriptValue.Boolean(
                            interpretAsObject(interpret(statement.rhs)).enumerableKeys.any { key ->
                                JavascriptValue.looselyEquals(JavascriptValue.String(key), lhsValue)
                            }
                        ).toReference()
                    }
                    is JavascriptTokenType.InstanceOf -> {
                        val prototypeToFind = when (val rhsValue = interpret(statement.rhs)) {
                            is JavascriptValue.Object -> when (val constructor = rhsValue.value) {
                                is FunctionObject -> constructor.functionPrototype
                                else -> {
                                    throwTypeError("Right-hand side of 'instanceof' is not callable")
                                    return JavascriptReference.Undefined
                                }
                            }
                            else -> {
                                throwTypeError("Right-hand side of 'instanceof' is not an object")
                                return JavascriptReference.Undefined
                            }
                        }

                        val isInstanceOf = when (val lhsValue = interpret(statement.lhs)) {
                            is JavascriptValue.Object -> lhsValue.value.prototypeChain.contains(prototypeToFind)
                            else -> false
                        }

                        return JavascriptValue.Boolean(isInstanceOf).toReference()
                    }
                    else -> interpretBinaryOperator(binaryExpression = statement)
                }
            }
            is JavascriptExpression.UnaryOperation -> {
                return when (statement.operator) {
                    is JavascriptTokenType.TypeOf -> {
                        JavascriptValue.String(interpret(statement.expression).typeName).toReference()
                    }
                    is JavascriptTokenType.Void -> {
                        interpret(statement.expression)
                        JavascriptReference.Undefined
                    }
                    is JavascriptTokenType.Delete -> {
                        val reference = interpretAsReference(statement.expression)

                        return if (reference.deleter == null) {
                            JavascriptValue.Boolean(false)
                        } else {
                            reference.deleter.invoke()
                            JavascriptValue.Boolean(true)
                        }.toReference()
                    }
                    is JavascriptTokenType.Operator.Not -> {
                        JavascriptValue.Boolean(!interpret(statement.expression).isTruthy).toReference()
                    }
                    is JavascriptTokenType.Operator.Minus -> {
                        interpret(
                            JavascriptExpression.BinaryOperation(
                                sourceInfo = statement.sourceInfo,
                                operator = JavascriptTokenType.Operator.Multiply,
                                lhs = statement.expression,
                                rhs = JavascriptExpression.Literal(sourceInfo = statement.sourceInfo, value = JavascriptValue.Number(-1.0))
                            )
                        ).toReference()
                    }
                    is JavascriptTokenType.Operator.Plus -> {
                        JavascriptValue.Number(interpret(statement.expression).coerceToNumber()).toReference()
                    }
                    is JavascriptTokenType.PlusPlus -> {
                        val reference = interpretAsReference(statement.expression)
                        val newValue = interpret(
                            JavascriptExpression.BinaryOperation(
                                sourceInfo = statement.sourceInfo,
                                operator = JavascriptTokenType.Operator.Plus,
                                lhs = JavascriptExpression.Literal(sourceInfo = statement.sourceInfo, value = reference.value),
                                rhs = JavascriptExpression.Literal(sourceInfo = statement.sourceInfo, value = JavascriptValue.Number(1.0))
                            )
                        )
                        reference.setter?.invoke(newValue)
                            ?: error("Uncaught SyntaxError: Invalid left-hand side in assignment")

                        if (statement.isPrefix) {
                            newValue.toReference()
                        } else {
                            reference.value.toReference()
                        }
                    }
                    is JavascriptTokenType.MinusMinus -> {
                        val reference = interpretAsReference(statement.expression)
                        val newValue = interpret(
                            JavascriptExpression.BinaryOperation(
                                sourceInfo = statement.sourceInfo,
                                operator = JavascriptTokenType.Operator.Minus,
                                lhs = JavascriptExpression.Literal(sourceInfo = statement.sourceInfo, value = reference.value),
                                rhs = JavascriptExpression.Literal(sourceInfo = statement.sourceInfo, value = JavascriptValue.Number(1.0))
                            )
                        )

                        reference.setter?.invoke(newValue)
                            ?: error("Uncaught SyntaxError: Invalid left-hand side in assignment")

                        if (statement.isPrefix) {
                            newValue.toReference()
                        } else {
                            reference.value.toReference()
                        }
                    }
                    else -> {
                        error("${statement.operator} is unsupported for Uunary operations.")
                    }
                }
            }
            is JavascriptExpression.Reference -> {
                return currentScope.getVariable(statement.name)
            }
            is JavascriptExpression.DotAccess -> {
                val objectToAccess = interpretAsObject(statement.expression)

                return objectToAccess.getProperty(statement.propertyName).toReference(
                    deleter = {
                        objectToAccess.deleteProperty(statement.propertyName)
                    },
                    setter = {
                        objectToAccess.setProperty(statement.propertyName, it)
                    }
                )
            }
            is JavascriptExpression.IndexAccess -> {
                val objectToAccess = interpretAsObject(statement.expression)

                val property = interpret(statement.indexExpression)
                return objectToAccess.getProperty(property.toString()).toReference(
                    deleter = {
                        objectToAccess.deleteProperty(property.toString())
                    },
                    setter = {
                        objectToAccess.setProperty(property.toString(), it)
                    }
                )
            }
            is JavascriptExpression.Literal -> {
                return statement.value.toReference()
            }
            is JavascriptExpression.ObjectLiteral -> {
                val newObject = makeObject()

                for (field in statement.fields) {
                    newObject.setProperty(field.name, interpret(field.rhs))

                    if (hasControlFlowInterrupted()) {
                        return JavascriptReference.Undefined
                    }
                }

                return JavascriptValue.Object(newObject).toReference()
            }
            is JavascriptExpression.ArrayLiteral -> {
                return JavascriptValue.Object(
                    ArrayObject(this, statement.items.map { interpret(it) })
                ).toReference()
            }
            is JavascriptExpression.RegexLiteral -> {
                return JavascriptValue.Object(
                    RegExpObject(this, statement.pattern, statement.flags)
                ).toReference()
            }
            is JavascriptExpression.AnonymousFunction -> {
                return JavascriptValue.Object(
                    JavascriptFunction(
                        interpreter = this,
                        name = "anonymous",
                        parameterNames = statement.parameterNames,
                        body = statement.body,
                        parentScope = currentScope
                    )
                ).toReference()
            }
            is JavascriptExpression.ArrowFunction -> {
                return JavascriptValue.Object(
                    JavascriptFunction(
                        interpreter = this,
                        name = "anonymous",
                        parameterNames = statement.parameterNames,
                        body = if (statement.body is JavascriptStatement.Block) {
                            statement.body
                        } else {
                            val expression = statement.body as JavascriptExpression
                            JavascriptStatement.Block(
                                sourceInfo = expression.sourceInfo,
                                body = listOf(
                                    JavascriptStatement.Return(
                                        sourceInfo = expression.sourceInfo,
                                        expression = expression
                                    )
                                )
                            )
                        },
                        parentScope = currentScope
                    ).apply {
                        boundThis = stack.peek().scope.thisBinding
                    }
                ).toReference()
            }
            is JavascriptExpression.NewCall -> {
                return when (val value = interpret(statement.function.expression)) {
                    is JavascriptValue.Object -> {
                        when (val constructor = value.value) {
                            is FunctionObject -> {
                                val arguments = statement.function.parameters.map {
                                    interpret(it).also {
                                        if (hasControlFlowInterrupted()) {
                                            return JavascriptReference.Undefined
                                        }
                                    }
                                }

                                val objectThis = JavascriptObject(prototype = constructor.functionPrototype)
                                val nativeExecutionContext = NativeExecutionContext(
                                    callLocation = statement.sourceInfo,
                                    arguments = arguments,
                                    thisBinding = objectThis,
                                    interpreter = this
                                )

                                val returnValue = constructor.call(nativeExecutionContext)

                                when {
                                    hasControlFlowInterrupted() -> JavascriptValue.Undefined
                                    returnValue is JavascriptValue.Object -> returnValue
                                    else -> JavascriptValue.Object(objectThis)
                                }.toReference()
                            }
                            else -> {
                                val constructorDescription = describeFunctionTarget(statement.function.expression)
                                throwTypeError("$constructorDescription is not a constructor")
                                JavascriptReference.Undefined
                            }
                        }
                    }
                    else -> {
                        val constructorDescription = describeFunctionTarget(statement.function.expression)
                        throwTypeError("$constructorDescription is not a constructor")
                        JavascriptReference.Undefined
                    }
                }
            }
        }
    }

    private fun interpretPrimitiveValueOf(expression: JavascriptExpression): JavascriptValue {
        fun interpretExpressionThenInterpretPrimitiveValue(): JavascriptValue {
            val primitiveFunctionNames = listOf("valueOf", "toString")

            when (val value = interpret(expression)) {
                is JavascriptValue.Object -> {
                    for (functionName in primitiveFunctionNames) {
                        if (value.value.getProperty(functionName).asFunction() != null) {
                            val result = interpret(
                                JavascriptExpression.FunctionCall(
                                    sourceInfo = expression.sourceInfo,
                                    expression = JavascriptExpression.DotAccess(
                                        sourceInfo = expression.sourceInfo,
                                        expression = JavascriptExpression.Literal(expression.sourceInfo, value),
                                        propertyName = functionName
                                    ),
                                    parameters = emptyList()
                                )
                            )

                            if (result !is JavascriptValue.Object) {
                                return result
                            }
                        }
                    }

                    throwTypeError("Cannot convert object to primitive value")
                    return JavascriptValue.Undefined
                }
                else -> return value
            }
        }

        return when (expression) {
            is JavascriptExpression.Literal -> {
                when (expression.value) {
                    is JavascriptValue.Object -> interpretExpressionThenInterpretPrimitiveValue()
                    else -> expression.value
                }
            }
            else -> interpretExpressionThenInterpretPrimitiveValue()
        }
    }

    fun interpretAsObject(expression: JavascriptExpression): JavascriptObject {
        return interpretAsObject(value = interpret(expression))
    }

    fun interpretAsObject(value: JavascriptValue): JavascriptObject {
        return when (value) {
            is JavascriptValue.Object -> value.value
            is JavascriptValue.String -> StringObject(interpreter = this, value = value.value)
            is JavascriptValue.Number -> NumberObject(interpreter = this, value = value.value)
            else -> makeObject()
        }
    }

    fun interpretAsString(expression: JavascriptExpression): String {
        return interpretPrimitiveValueOf(expression).asString() ?: ""
    }

    fun interpretAsString(value: JavascriptValue): String {
        return interpretPrimitiveValueOf(JavascriptExpression.Literal(sourceInfo = SourceInfo(0, 0), value = value)).asString() ?: ""
    }

    private fun interpretBinaryOperator(
        binaryExpression: JavascriptExpression.BinaryOperation
    ): JavascriptReference {
        val lhsValue = interpretPrimitiveValueOf(binaryExpression.lhs)
        if (hasControlFlowInterrupted()) return JavascriptReference.Undefined

        val rhsValue = interpretPrimitiveValueOf(binaryExpression.rhs)
        if (hasControlFlowInterrupted()) return JavascriptReference.Undefined

        return when (binaryExpression.operator) {
            is JavascriptTokenType.Operator.Plus -> {
                if (lhsValue is JavascriptValue.String || rhsValue is JavascriptValue.String) {
                    JavascriptValue.String(lhsValue.toString() + rhsValue.toString())
                } else {
                    JavascriptValue.Number(lhsValue.coerceToNumber() + rhsValue.coerceToNumber())
                }
            }
            is JavascriptTokenType.Operator.Minus -> {
                JavascriptValue.Number(lhsValue.coerceToNumber() - rhsValue.coerceToNumber())
            }
            is JavascriptTokenType.Operator.Multiply -> {
                JavascriptValue.Number(lhsValue.coerceToNumber() * rhsValue.coerceToNumber())
            }
            is JavascriptTokenType.Operator.Divide -> {
                JavascriptValue.Number(lhsValue.coerceToNumber() / rhsValue.coerceToNumber())
            }
            is JavascriptTokenType.Operator.Xor -> {
                val result = (
                    lhsValue.coerceToNumber().toInt() xor rhsValue.coerceToNumber().toInt()
                ).toDouble()

                JavascriptValue.Number(result)
            }
            is JavascriptTokenType.Operator.Or -> {
                val result = (
                    lhsValue.coerceToNumber().toInt() or rhsValue.coerceToNumber().toInt()
                ).toDouble()

                JavascriptValue.Number(result)
            }
            is JavascriptTokenType.Operator.And -> {
                val result = (
                    lhsValue.coerceToNumber().toInt() and rhsValue.coerceToNumber().toInt()
                ).toDouble()

                JavascriptValue.Number(result)
            }
            is JavascriptTokenType.Operator.Mod -> {
                JavascriptValue.Number(lhsValue.coerceToNumber() % rhsValue.coerceToNumber())
            }
            is JavascriptTokenType.Operator.LeftShift -> {
                val result = (
                    lhsValue.coerceToNumber().toInt() shl rhsValue.coerceToNumber().toInt()
                ).toDouble()

                JavascriptValue.Number(result)
            }
            is JavascriptTokenType.Operator.RightShift -> {
                val result = (
                    lhsValue.coerceToNumber().toInt() shr rhsValue.coerceToNumber().toInt()
                ).toDouble()

                JavascriptValue.Number(result)
            }
            is JavascriptTokenType.Operator.LessThanOrEqual -> {
                JavascriptValue.Boolean(lhsValue.coerceToNumber() <= rhsValue.coerceToNumber())
            }
            is JavascriptTokenType.Operator.LessThan -> {
                JavascriptValue.Boolean(lhsValue.coerceToNumber() < rhsValue.coerceToNumber())
            }
            is JavascriptTokenType.Operator.GreaterThanOrEqual -> {
                JavascriptValue.Boolean(lhsValue.coerceToNumber() >= rhsValue.coerceToNumber())
            }
            is JavascriptTokenType.Operator.GreaterThan -> {
                JavascriptValue.Boolean(lhsValue.coerceToNumber() > rhsValue.coerceToNumber())
            }
            is JavascriptTokenType.Operator.Equals -> {
                JavascriptValue.Boolean(JavascriptValue.looselyEquals(lhsValue, rhsValue))
            }
            is JavascriptTokenType.Operator.NotEquals -> {
                JavascriptValue.Boolean(!JavascriptValue.looselyEquals(lhsValue, rhsValue))
            }

            else -> error("Attempted to interpret unknown binary operator: ${binaryExpression.operator}")
        }.toReference()
    }

    private fun interpretOperatorAssignAsReference(
        newOperator: JavascriptTokenType.Operator,
        binaryExpression: JavascriptExpression.BinaryOperation
    ): JavascriptReference {
        val reference = interpretAsReference(binaryExpression.lhs)

        val valueToAssign = interpret(
            JavascriptExpression.BinaryOperation(
                sourceInfo = binaryExpression.sourceInfo,
                operator = newOperator,
                lhs = JavascriptExpression.Literal(binaryExpression.sourceInfo, interpretPrimitiveValueOf(JavascriptExpression.Literal(binaryExpression.sourceInfo, reference.value))),
                rhs = binaryExpression.rhs
            )
        )

        reference.setter?.invoke(valueToAssign)
            ?: error("Uncaught SyntaxError: Invalid left-hand side in assignment")

        return valueToAssign.toReference()
    }

    fun makeObject(prototype: JavascriptObject = objectPrototype): JavascriptObject {
        return JavascriptObject(prototype = prototype, interpreter = this)
    }

    fun makeArray(initialValues: List<JavascriptValue> = emptyList()): ArrayObject {
        return ArrayObject(this, initialValues)
    }

    fun interpretFunction(
        callLocation: SourceInfo,
        arguments: List<JavascriptValue>,
        thisBinding: JavascriptObject,
        javascriptFunction: JavascriptFunction
    ): JavascriptValue {
        enterFunction(
            sourceInfo = callLocation,
            passedParameters = arguments,
            thisBinding = thisBinding,
            functionName = javascriptFunction.name,
            parameterNames = javascriptFunction.parameterNames,
            parentScope = javascriptFunction.parentScope
        )

        interpret(javascriptFunction.body)

        exitFunction()

        return maybeConsumeControlFlowInterrupt<ControlFlowInterruption.Return>()?.value ?: JavascriptValue.Undefined
    }

    private fun enterFunction(
        sourceInfo: SourceInfo,
        functionName: String,
        parameterNames: List<String>,
        passedParameters: List<JavascriptValue>,
        parentScope: JavascriptScope,
        thisBinding: JavascriptObject? = null
    ) {
        val functionScope = JavascriptScope(
            thisBinding = thisBinding ?: parentScope.thisBinding,
            parentScope = parentScope,
            globalObject = globalObject,
            type = JavascriptScope.Type.Function
        ).apply {
            setVariable("arguments", JavascriptValue.Object(ArrayObject(this@JavascriptInterpreter, passedParameters)))
            parameterNames.forEachIndexed { index, parameterName ->
                setVariable(
                    parameterName,
                    passedParameters.getOrElse(index) { JavascriptValue.Undefined }
                )
            }
        }

        stack.peek().sourceInfo = sourceInfo

        stack.push(
            JavascriptStackFrame(
                name = functionName,
                scope = functionScope,
                sourceInfo = sourceInfo
            )
        )
    }

    private fun exitFunction() {
        stack.pop()
    }

    private fun describeFunctionTarget(target: JavascriptExpression): String {
        return when (target) {
            is JavascriptExpression.DotAccess -> "${describeFunctionTarget(target.expression)}.${target.propertyName}"
            is JavascriptExpression.IndexAccess -> "${describeFunctionTarget(target.expression)}[${describeFunctionTarget(target.expression)}]"
            is JavascriptExpression.Reference -> target.name
            is JavascriptExpression.FunctionCall -> "${describeFunctionTarget(target.expression)}(${target.parameters.map { describeFunctionTarget(it) }.joinToString(", ")})"
            else -> when (val value = interpret(target)) {
                is JavascriptValue.String -> "\"${value.toString()}\""
                else -> value.toString()
            }
        }
    }

    private fun enterScope(
        parameterNames: List<String> = emptyList(),
        passedParameters: List<JavascriptExpression> = emptyList()
    ) {
        stack.peek().scope = JavascriptScope(
            thisBinding = stack.peek().scope.thisBinding,
            globalObject = globalObject,
            parentScope = stack.peek().scope
        ).apply {
            parameterNames.forEachIndexed { index, parameterName ->
                setVariable(
                    parameterName,
                    interpret(
                        passedParameters.getOrElse(index) {
                            JavascriptExpression.Literal(sourceInfo = SourceInfo(0, 0), value = JavascriptValue.Undefined)
                        }
                    )
                )
            }
        }
    }

    private fun exitScope() {
        stack.peek().scope = currentScope.parentScope ?: JavascriptScope(
            thisBinding = globalObject,
            globalObject = globalObject,
            parentScope = null
        )
    }

    private fun interruptControlFlowWith(interruption: ControlFlowInterruption) {
        controlFlowInterruption = interruption
    }

    private inline fun <reified T : ControlFlowInterruption> maybeConsumeControlFlowInterrupt(): T? {
        if (controlFlowInterruption !is T) {
            return null
        }
        return (controlFlowInterruption as T).also { controlFlowInterruption = null }
    }

    private inline fun <reified T : ControlFlowInterruption> consumeControlFlowInterrupt(): T {
        return (controlFlowInterruption as T).also { controlFlowInterruption = null }
    }

    private fun hasControlFlowInterrupted(): Boolean {
        return controlFlowInterruption != null
    }

    private inline fun <reified T : ControlFlowInterruption> hasControlFlowInterruptedDueTo(): Boolean {
        return controlFlowInterruption is T
    }

    fun throwTypeError(message: String) {
        val sourceInfo = stack.peek().sourceInfo
        throwError(
            interpret(
                JavascriptExpression.NewCall(
                    sourceInfo = sourceInfo,
                    function = JavascriptExpression.FunctionCall(
                        sourceInfo = sourceInfo,
                        expression = JavascriptExpression.Reference(sourceInfo, "TypeError"),
                        parameters = listOf(JavascriptExpression.Literal(sourceInfo, JavascriptValue.String(message)))
                    )
                )
            )
        )
    }

    fun throwError(error: JavascriptValue) {
        interruptControlFlowWith(
            ControlFlowInterruption.Error(
                value = error,
                trace = stack.map { it.copy() }.reversed()
            )
        )
    }

    private fun initializeGlobalObject() {
        globalObject.apply {
            setNonEnumerableNativeFunction("getInput") { executionContext ->
                val inputText = executionContext.arguments.firstOrNull() as? JavascriptValue.String
                if (inputText != null) {
                    print(inputText.value)
                }
                JavascriptValue.Number((readLine() ?: "").toDouble())
            }

            setNonEnumerableNativeFunction("parseInt") { executionContext ->
                JavascriptValue.Number(executionContext.arguments.first().coerceToNumber().toInt().toDouble())
            }

            setProperty(
                "console",
                JavascriptValue.Object(
                    makeObject().apply {
                        setNonEnumerableNativeFunction("log") { executionContext ->
                            println(executionContext.arguments.joinToString(separator = " "))
                            JavascriptValue.Undefined
                        }
                    }
                )
            )

            setProperty(
                "Math",
                JavascriptValue.Object(
                    makeObject().apply {
                        setNonEnumerableNativeFunction("floor") { executionContext ->
                            JavascriptValue.Number(executionContext.arguments.first().coerceToNumber().toInt().toDouble())
                        }

                        setNonEnumerableNativeFunction("random") {
                            JavascriptValue.Number(Random.nextDouble())
                        }
                    }
                )
            )
        }
    }

    sealed class ControlFlowInterruption {
        data class Return(val value: JavascriptValue) : ControlFlowInterruption()
        data class Break(val label: String?) : ControlFlowInterruption()
        data class Continue(val label: String?) : ControlFlowInterruption()

        data class Error(
            val value: JavascriptValue,
            val trace: List<JavascriptStackFrame>
        ) : ControlFlowInterruption()
    }
}
