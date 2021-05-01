package ca.antonious.browser.libraries.javascript.interpreter.builtins.function

import ca.antonious.browser.libraries.javascript.ast.ClassBody
import ca.antonious.browser.libraries.javascript.ast.JavascriptStatement
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptScope

open class ClassConstructor(
    interpreter: JavascriptInterpreter,
    name: String,
    parameterNames: List<String>,
    body: JavascriptStatement.Block,
    parentScope: JavascriptScope,
    private val classMembers: List<ClassBody.Statement.Member>
) : JavascriptFunction(
    interpreter = interpreter,
    name = name,
    parameterNames = parameterNames,
    body = body,
    parentScope = parentScope
) {

    override fun call(nativeExecutionContext: NativeExecutionContext): JavascriptValue {
        for (member in classMembers) {
            nativeExecutionContext.thisBinding.setProperty(member.name, nativeExecutionContext.interpreter.interpret(member.expression))
        }

        return nativeExecutionContext.interpreter.interpretFunction(
            callLocation = nativeExecutionContext.callLocation,
            thisBinding = nativeExecutionContext.thisBinding,
            arguments = nativeExecutionContext.arguments,
            javascriptFunction = this
        )
    }

    override fun toString(): String {
        return "class (${parameterNames.joinToString(", ")}) {...}"
    }
}
