package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.builtins.function.FunctionObject
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

    private val descriptors = mutableMapOf<String, JavascriptPropertyDescriptor>()

    val allPropertyKeys: List<String>
        get() {
            return descriptors.map { it.key }
        }

    val allProperties: List<Pair<String, JavascriptValue>>
        get() {
            return descriptors
                .map { it.key to getProperty(it.key) }
        }

    val enumerableKeys: List<String>
        get() {
            return descriptors
                .filter { it.value.enumerable }
                .map { it.key }
        }

    val enumerableProperties: List<Pair<String, JavascriptValue>>
        get() {
            return descriptors
                .filter { it.value.enumerable }
                .map { it.key to getProperty(it.key) }
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
                descriptor = JavascriptPropertyDescriptor(
                    value = JavascriptValue.Object(prototype),
                    enumerable = false,
                    configurable = false
                )
            )
        }
    }

    open fun initialize() = Unit

    open fun getProperty(key: String): JavascriptValue {
        val ownPropertyDescriptor = descriptors[key]

        return if (ownPropertyDescriptor != null) {
            when {
                ownPropertyDescriptor.value != null -> ownPropertyDescriptor.value!!
                ownPropertyDescriptor.get != null -> ownPropertyDescriptor.get!!.call(
                    NativeExecutionContext(
                        callLocation = interpreter.stack.peek().sourceInfo,
                        arguments = emptyList(),
                        thisBinding = this,
                        interpreter = interpreter
                    )
                )
                ownPropertyDescriptor.set != null -> JavascriptValue.Undefined
                else -> error("Attempted to get existing property with no value or getter/setter")
            }
        } else {
            prototype?.getProperty(key) ?: JavascriptValue.Undefined
        }
    }

    open fun setProperty(key: String, value: JavascriptValue) {
        val ownPropertyDescriptor = descriptors[key]

        if (ownPropertyDescriptor != null) {
            when {
                ownPropertyDescriptor.value != null -> ownPropertyDescriptor.value = value
                ownPropertyDescriptor.set != null -> ownPropertyDescriptor.set!!.call(
                    NativeExecutionContext(
                        callLocation = interpreter.stack.peek().sourceInfo,
                        arguments = listOf(value),
                        thisBinding = this,
                        interpreter = interpreter
                    )
                )
                ownPropertyDescriptor.get != null -> return
                else -> error("Attempted to set existing property with no value or getter/setter")
            }
        } else {
            setProperty(key, JavascriptPropertyDescriptor(value))
        }
    }

    open fun deleteProperty(key: String) {
        descriptors.remove(key)
    }

    fun setNonEnumerableProperty(key: String, value: JavascriptValue) {
        setProperty(key, JavascriptPropertyDescriptor(value, enumerable = false))
    }

    fun setNonEnumerableNativeFunction(name: String, body: (NativeExecutionContext) -> JavascriptValue) {
        setNonEnumerableProperty(
            name,
            JavascriptValue.Object(
                NativeFunction(interpreter, interpreter.makeObject(), body)
            )
        )
    }

    fun setNonEnumerableNativeGetter(name: String, body: (NativeExecutionContext) -> JavascriptValue) {
        descriptors[name] = JavascriptPropertyDescriptor(
            get = NativeFunction(interpreter, interpreter.makeObject(), body),
            enumerable = false
        )
    }

    fun setNonEnumerableNativeSetter(name: String, body: (NativeExecutionContext) -> JavascriptValue) {
        descriptors[name] = JavascriptPropertyDescriptor(
            set = NativeFunction(interpreter, interpreter.makeObject(), body),
            enumerable = false
        )
    }

    fun setProperty(key: String, descriptor: JavascriptPropertyDescriptor) {
        descriptors[key] = descriptor
    }

    override fun toString(): String {
        return "Object {${descriptors.entries.joinToString(separator = ", ") {
            val valueString = when (it.value.value) {
                is JavascriptValue.Object -> "Object"
                else -> it.value.value.toString()
            }
            "${it.key}: $valueString"
        }}}"
    }

    fun hasOwnProperty(propertyName: String): Boolean {
        return propertyName in descriptors
    }

    fun getOwnPropertyDescriptor(key: String): JavascriptPropertyDescriptor? {
        return descriptors[key]
    }
}

data class JavascriptPropertyDescriptor(
    var value: JavascriptValue? = null,
    var get: FunctionObject? = null,
    var set: FunctionObject? = null,
    var writable: Boolean = true,
    var configurable: Boolean = true,
    var enumerable: Boolean = true
) {
    val isDataDescriptor: Boolean
        get() {
            return value != null
        }

    val isAccessorDescriptor: Boolean
        get() {
            return get != null || set != null
        }
}
