package ca.antonious.browser.libraries.javascript.interpreter.builtins.function

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

class BoundFunction(
    override var boundThis: JavascriptObject?,
    private val wrappedFunction: FunctionObject
) : FunctionObject(wrappedFunction.interpreter, wrappedFunction.functionPrototype) {

    override fun call(nativeExecutionContext: NativeExecutionContext): JavascriptValue {
        return wrappedFunction.call(nativeExecutionContext)
    }

    override fun toString(): String {
        return wrappedFunction.toString()
    }
}
