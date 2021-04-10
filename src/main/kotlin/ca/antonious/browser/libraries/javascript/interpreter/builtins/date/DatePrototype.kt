package ca.antonious.browser.libraries.javascript.interpreter.builtins.date

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

class DatePrototype(interpreter: JavascriptInterpreter) : JavascriptObject(interpreter.objectPrototype) {
    override fun initialize() {
        super.initialize()

        setNonEnumerableNativeFunction("valueOf") { nativeExecutionContext ->
            val dateObject = nativeExecutionContext.thisBinding as? DateObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            JavascriptValue.Number(dateObject.date.time.toDouble())
        }
    }
}
