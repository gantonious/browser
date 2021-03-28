package ca.antonious.browser.libraries.javascript.interpreter.builtins.function

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.array.JavascriptArray

object FunctionPrototype : JavascriptObject() {
    init {
        setNonEnumerableNativeFunction("call") { executionContext ->
            val functionObject = executionContext.thisBinding as? FunctionObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val thisBindingOverride = executionContext.interpreter.interpretAsObject(executionContext.arguments.first())
            val argumentsArray =executionContext.arguments.drop(1)

            val callExecutionContext = executionContext.copy(
                thisBinding = thisBindingOverride,
                arguments = argumentsArray,
            )

            functionObject.call(callExecutionContext)
        }

        setNonEnumerableNativeFunction("apply") { executionContext ->
            val functionObject = executionContext.thisBinding as? FunctionObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val thisBindingOverride = executionContext.interpreter.interpretAsObject(executionContext.arguments.first())

            val argumentsArray = if (executionContext.arguments.size == 1) {
                JavascriptArray()
            } else {
                val array = executionContext.arguments[1].valueAs<JavascriptValue.Object>()?.value as? JavascriptArray

                if (array == null) {
                    executionContext.interpreter.throwError(JavascriptValue.String("Apply: Expected second argument to be an array."))
                    return@setNonEnumerableNativeFunction JavascriptValue.Undefined
                }

                array
            }

            val applyExecutionContext = executionContext.copy(
                thisBinding = thisBindingOverride,
                arguments = argumentsArray.array,
            )

            functionObject.call(applyExecutionContext)
        }
    }
}
