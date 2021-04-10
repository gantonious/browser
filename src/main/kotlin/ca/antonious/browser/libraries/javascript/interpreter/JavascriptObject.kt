package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeExecutionContext
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.NativeFunction

open class JavascriptObject(
    val interpreter: JavascriptInterpreter,
    val prototype: JavascriptObject?
) {
    constructor(prototype: JavascriptObject) : this(
        interpreter = prototype.interpreter,
        prototype = prototype
    )

    private val properties = mutableMapOf<String, JavascriptProperty>()

    val allPropertyKeys: List<String>
        get() {
            return properties.map { it.key }
        }

    val allProperties: List<Pair<String, JavascriptValue>>
        get() {
            return properties
                .map { it.key to it.value.value }
        }

    val enumerableKeys: List<String>
        get() {
            return properties
                .filter { it.value.enumerable }
                .map { it.key }
        }

    val enumerableProperties: List<Pair<String, JavascriptValue>>
        get() {
            return properties
                .filter { it.value.enumerable }
                .map { it.key to it.value.value }
        }

    val prototypeChain: List<JavascriptObject>
        get() {
            val chain = mutableListOf<JavascriptObject>()
            var currentPrototype = prototype

            while (currentPrototype != null) {
                chain += currentPrototype
                currentPrototype = currentPrototype.prototype
            }

            return chain
        }

    init {
        if (prototype != null) {
            setProperty(
                key = "__proto__",
                property = JavascriptProperty(
                    value = JavascriptValue.Object(prototype),
                    enumerable = false,
                    configurable = false
                )
            )
        }
    }

    open fun initialize() = Unit

    open fun getProperty(key: String): JavascriptValue {
        return properties[key]?.value ?:
            prototype?.getProperty(key) ?:
            JavascriptValue.Undefined
    }

    open fun setProperty(key: String, value: JavascriptValue) {
        setProperty(key, JavascriptProperty(value))
    }

    open fun deleteProperty(key: String) {
        properties.remove(key)
    }

    fun setNonEnumerableProperty(key: String, value: JavascriptValue) {
        setProperty(key, JavascriptProperty(value, enumerable = false))
    }

    fun setNonEnumerableNativeFunction(name: String, body: (NativeExecutionContext) -> JavascriptValue) {
        setNonEnumerableProperty(
            name,
            JavascriptValue.Object(
                NativeFunction(interpreter, interpreter.makeObject(), body)
            )
        )
    }

    fun setProperty(key: String, property: JavascriptProperty) {
        properties[key] = property
    }

    override fun toString(): String {
        return "Object {${properties.entries.joinToString(separator = ", ") {
            val valueString = when (it.value.value) {
                is JavascriptValue.Object -> "Object"
                else -> it.value.value.toString()
            }
            "${it.key}: $valueString"
        }}}"
    }
}

data class JavascriptProperty(
    var value: JavascriptValue,
    var configurable: Boolean = true,
    var writable: Boolean = true,
    var enumerable: Boolean = true
)
