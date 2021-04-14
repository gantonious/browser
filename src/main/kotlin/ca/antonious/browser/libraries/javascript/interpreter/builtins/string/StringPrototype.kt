package ca.antonious.browser.libraries.javascript.interpreter.builtins.string

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.array.ArrayObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.FunctionObject
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.JavascriptFunction
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction
import ca.antonious.browser.libraries.javascript.interpreter.builtins.regex.RegExpObject

class StringPrototype(interpreter: JavascriptInterpreter) : JavascriptObject(interpreter.objectPrototype) {
    override fun initialize() {
        super.initialize()

        setNonEnumerableNativeFunction("match") { executionContext ->
            val stringObject = executionContext.thisBinding as? StringObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val regex = executionContext.arguments.first().valueAs<JavascriptValue.Object>()?.value as RegExpObject
            JavascriptValue.Object(
                interpreter.makeArray(
                    Regex(regex.regex).findAll(stringObject.value).map { JavascriptValue.String(it.value) }.toList()
                )
            )
        }

        setNonEnumerableNativeFunction("toLowerCase") { executionContext ->
            val stringObject = executionContext.thisBinding as? StringObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            JavascriptValue.String(stringObject.value.toLowerCase())
        }

        setNonEnumerableNativeFunction("toUpperCase") { executionContext ->
            val stringObject = executionContext.thisBinding as? StringObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            JavascriptValue.String(stringObject.value.toUpperCase())
        }

        setNonEnumerableNativeFunction("charAt") { executionContext ->
            val stringObject = executionContext.thisBinding as? StringObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val index = executionContext.arguments.firstOrNull()?.coerceToNumber()?.toInt() ?: 0

            if (index >= 0 && index < stringObject.value.length) {
                JavascriptValue.String(stringObject.value[index].toString())
            } else {
                JavascriptValue.String("")
            }
        }

        setNonEnumerableNativeFunction("replace") { executionContext ->
            val stringObject = executionContext.thisBinding as? StringObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            if (executionContext.arguments.isEmpty()) {
                return@setNonEnumerableNativeFunction JavascriptValue.String(stringObject.value)
            }

            val patternToMatch = when (val textToMatch = executionContext.arguments.first()) {
                is JavascriptValue.Object -> {
                    when (val objectValue = textToMatch.value) {
                        is StringObject -> objectValue.value
                        is RegExpObject -> objectValue.regex
                        else -> objectValue.toString()
                    }
                }
                else -> textToMatch.toString()
            }

            val replacer = executionContext.arguments.getOrNull(1) ?: JavascriptValue.Undefined

            val valueToReplace = when (replacer) {
                is JavascriptValue.Object -> replacer.value
                else -> replacer
            }

            val patternRegex = Regex(patternToMatch)

            if (valueToReplace is FunctionObject) {
                var hasMatched = false
                val result = patternRegex.replace(stringObject.value) { matchResult ->
                    if (!hasMatched) {
                        hasMatched = true
                        valueToReplace.call(
                            executionContext.copy(
                                arguments = listOf(
                                    JavascriptValue.String(matchResult.value)
                                ) + matchResult.groupValues.drop(1).map { groupValue ->
                                    JavascriptValue.String(groupValue)
                                } + listOf(
                                    JavascriptValue.Number(matchResult.range.first.toDouble()),
                                    JavascriptValue.String(stringObject.value)
                                )
                            )
                        ).toPrimitiveString()
                    } else {
                        matchResult.value
                    }
                }

                JavascriptValue.String(result)
            } else {
                JavascriptValue.String(patternRegex.replaceFirst(stringObject.value, valueToReplace.toString()))
            }
        }

        setNonEnumerableNativeFunction("split") { executionContext ->
            val stringObject = executionContext.thisBinding as? StringObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val patternToMatch = when (val textToMatch = executionContext.arguments.first()) {
                is JavascriptValue.Object -> {
                    when (val objectValue = textToMatch.value) {
                        is StringObject -> objectValue.value
                        is RegExpObject -> objectValue.regex
                        else -> objectValue.toString()
                    }
                }
                else -> textToMatch.toString()
            }

            val limit = if (executionContext.arguments.size >= 2) {
                executionContext.arguments[1].coerceToNumber().toInt()
            } else {
                0
            }

            JavascriptValue.Object(
                interpreter.makeArray(
                    stringObject.value.split(Regex(patternToMatch), limit).map { JavascriptValue.String(it) }
                )
            )
        }

        setNonEnumerableNativeFunction("concat") { executionContext ->
            val stringObject = executionContext.thisBinding as? StringObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val stringsToConcat = executionContext.arguments.map { it.toString() }

            JavascriptValue.String(value = (listOf(stringObject.value) + stringsToConcat).joinToString())
        }

        setNonEnumerableNativeFunction("slice") { nativeExecutionContext ->
            val stringObject = nativeExecutionContext.thisBinding as? StringObject
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            var startIndex = (nativeExecutionContext.arguments.getOrNull(0)?.coerceToNumber() ?: 0).toInt()
            var endIndex = (nativeExecutionContext.arguments.getOrNull(1)?.coerceToNumber() ?: stringObject.value.length).toInt()

            if (startIndex < 0) {
                startIndex += stringObject.value.length
            }

            if (endIndex < 0) {
                endIndex += stringObject.value.length
            }

            JavascriptValue.String(stringObject.value.substring(startIndex, endIndex))
        }
    }
}
