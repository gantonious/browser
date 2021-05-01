package ca.antonious.browser.libraries.javascript.interpreter

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue

class JavascriptScope(
    var thisBinding: JavascriptObject,
    val parentScope: JavascriptScope?,
    val globalObject: JavascriptObject,
    val type: Type = Type.Block
) {

    val variables = mutableMapOf<String, JavascriptValue>()

    init {
        variables["this"] = JavascriptValue.Object(thisBinding)
    }

    fun getVariable(key: String): JavascriptReference {
        return getVariableIgnoringThis(key)
            ?: getVariableFromThisBinding(key)
            ?: globalObject.getProperty(key).toReference {
                globalObject.setProperty(key, it)
            }
    }

    private fun getVariableIgnoringThis(key: String): JavascriptReference? {
        return variables[key]?.toReference { setVariable(key, it) } ?: parentScope?.getVariableIgnoringThis(key)
    }

    private fun getVariableFromThisBinding(key: String): JavascriptReference? {
        return if (key in thisBinding.allPropertyKeys) {
            thisBinding.getProperty(key).toReference { thisBinding.setProperty(key, it) }
        } else {
            null
        }
    }

    fun setVariable(key: String, value: JavascriptValue) {
        variables[key] = value
    }

    sealed class Type {
        object Block : Type()
        object Function : Type()
    }
}
