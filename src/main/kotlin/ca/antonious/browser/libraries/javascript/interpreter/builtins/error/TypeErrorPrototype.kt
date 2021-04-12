package ca.antonious.browser.libraries.javascript.interpreter.builtins.error

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

class TypeErrorPrototype(interpreter: JavascriptInterpreter) : JavascriptObject(interpreter.errorPrototype) {
    override fun initialize() {
        setNonEnumerableProperty("name", JavascriptValue.String("TypeError"))
    }
}
