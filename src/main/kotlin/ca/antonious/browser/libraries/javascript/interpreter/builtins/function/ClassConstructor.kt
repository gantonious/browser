package ca.antonious.browser.libraries.javascript.interpreter.builtins.function

import ca.antonious.browser.libraries.javascript.ast.ClassBody
import ca.antonious.browser.libraries.javascript.ast.JavascriptStatement
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptScope

open class ClassConstructor(
    interpreter: JavascriptInterpreter,
    override val name: String,
    private val parameterNames: List<String>,
    private val body: JavascriptStatement.Block?,
    private val superConstructor: FunctionObject?,
    private val parentScope: JavascriptScope,
    private val classMembers: List<ClassBody.Statement.Member>
) : FunctionObject(
    interpreter = interpreter,
    functionPrototype = interpreter.makeObject()
) {

    init {
        functionPrototype.updatePrototype(superConstructor?.functionPrototype)
    }

    override fun call(nativeExecutionContext: NativeExecutionContext): JavascriptValue {
        var superObjectResult: JavascriptValue? = null

        nativeExecutionContext.interpreter.enterFunction(
            sourceInfo = nativeExecutionContext.callLocation,
            passedParameters = nativeExecutionContext.arguments,
            thisBinding = nativeExecutionContext.thisBinding,
            functionName = name,
            parameterNames = parameterNames,
            parentScope = parentScope
        )

        if (body == null) {
            val superResult = superConstructor?.call(nativeExecutionContext)

            if (superResult is JavascriptValue.Object) {
                superObjectResult = superResult
                nativeExecutionContext.interpreter.stack.peek().scope.thisBinding = superResult.value
            }
        } else {
            nativeExecutionContext.interpreter.interpret(body)
        }

        for (member in classMembers) {
            nativeExecutionContext.interpreter.stack.peek().scope.thisBinding.setProperty(member.name, nativeExecutionContext.interpreter.interpret(member.expression))
        }

        nativeExecutionContext.interpreter.exitFunction()

        val returnValue = superObjectResult ?:
            nativeExecutionContext.interpreter.maybeConsumeControlFlowInterrupt<JavascriptInterpreter.ControlFlowInterruption.Return>()?.value ?:
            JavascriptValue.Undefined

        if (returnValue is JavascriptValue.Object) {
            returnValue.value.updatePrototype(functionPrototype)
        }

        return returnValue
    }

    override fun toString(): String {
        return "class (${parameterNames.joinToString(", ")}) {...}"
    }
}
