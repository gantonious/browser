package ca.antonious.browser.libraries.javascript.interpreter.builtins.array

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeExecutionContext

class ArrayPrototype(interpreter: JavascriptInterpreter) : JavascriptObject(interpreter.objectPrototype) {
    override fun initialize() {
        setNonEnumerableNativeFunction("forEach") { nativeExecutionContext ->
            val function = nativeExecutionContext.arguments.firstOrNull()?.asFunction()

            if (function === null) {
                nativeExecutionContext.interpreter.throwError(JavascriptValue.String("TypeError: Argument is not a function"))
                return@setNonEnumerableNativeFunction JavascriptValue.Undefined
            }

            val array = nativeExecutionContext.thisBinding as? ArrayObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            array.array.forEach {
                function.call(
                    NativeExecutionContext(
                        callLocation = nativeExecutionContext.callLocation,
                        thisBinding = nativeExecutionContext.thisBinding,
                        interpreter = nativeExecutionContext.interpreter,
                        arguments = listOf(it)
                    )
                )
            }

            JavascriptValue.Undefined
        }
    }
}
