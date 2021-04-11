package ca.antonious.browser.libraries.javascript.interpreter.builtins.regex

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.array.ArrayObject

class RegExpObject(interpreter: JavascriptInterpreter, val regex: String, flags: String) : JavascriptObject(interpreter.regExpPrototype) {

    val flags = flags.windowed(1)

    override fun getProperty(key: String): JavascriptValue {
        return when (key) {
            "source" -> JavascriptValue.String(regex)
            else -> super.getProperty(key)
        }
    }

    override fun toString(): String {
        return "/$regex/$flags"
    }

    fun exec(input: String): JavascriptValue {
        val regex = Regex(regex)
        val matchResult = regex.find(input, 0) ?: return JavascriptValue.Null
        val matchedValues = mutableListOf(matchResult.value).apply {
            addAll(matchResult.groupValues)
        }

        return JavascriptValue.Object(
            ArrayObject(interpreter, matchedValues.map { JavascriptValue.String(it) }).apply {
                setProperty("index", JavascriptValue.Number(matchResult.range.first.toDouble()))
                setProperty("input", JavascriptValue.String(input))
                setProperty("groups", JavascriptValue.Undefined)
            }
        )
    }
}
