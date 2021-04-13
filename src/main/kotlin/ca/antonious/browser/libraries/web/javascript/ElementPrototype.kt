package ca.antonious.browser.libraries.web.javascript

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject
import ca.antonious.browser.libraries.web.layout.DOMElementNode

class ElementPrototype(interpreter: JavascriptInterpreter) : JavascriptObject(prototype = interpreter.objectPrototype) {
    init {
        setNonEnumerableNativeFunction("cloneNode") { nativeExecutionContext ->
            val htmlElement = nativeExecutionContext.thisBinding as? JavascriptHtmlElement
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val clonedElement = DOMElementNode(
                parent = null,
                htmlNode = htmlElement.domElementNode.htmlNode,
                domEventHandler = htmlElement.domElementNode.domEventHandler
            )

            JavascriptValue.Object(JavascriptHtmlElement(interpreter, clonedElement))
        }

        setNonEnumerableNativeFunction("getAttribute") { nativeExecutionContext ->
            val htmlElement = nativeExecutionContext.thisBinding as? JavascriptHtmlElement
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val attributeName = nativeExecutionContext.arguments.getOrNull(0)
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined.also {
                    nativeExecutionContext.interpreter.throwTypeError("Failed to execute 'setAttribute': missing key argument")
                }

            val attribute = htmlElement.domElementNode.attributes[attributeName.toPrimitiveString()]

            if (attribute == null) {
                JavascriptValue.Null
            } else {
                JavascriptValue.String(attribute)
            }
        }

        setNonEnumerableNativeFunction("setAttribute") { nativeExecutionContext ->
            val htmlElement = nativeExecutionContext.thisBinding as? JavascriptHtmlElement
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val attributeName = nativeExecutionContext.arguments.getOrNull(0)
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined.also {
                    nativeExecutionContext.interpreter.throwTypeError("Failed to execute 'setAttribute': missing key argument")
                }

            val attributeValue = nativeExecutionContext.arguments.getOrNull(1)
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined.also {
                    nativeExecutionContext.interpreter.throwTypeError("Failed to execute 'setAttribute': missing value argument")
                }

            htmlElement.domElementNode.setAttribute(attributeName.toPrimitiveString(), attributeValue.toPrimitiveString())

            JavascriptValue.Undefined
        }

        setNonEnumerableNativeFunction("getElementById") { nativeExecutionContext ->
            val htmlElement = nativeExecutionContext.thisBinding as? JavascriptHtmlElement
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val id = nativeExecutionContext.arguments.firstOrNull()

            if (id == null) {
                nativeExecutionContext.interpreter.throwTypeError("Failed to execute 'getElementById': 1 argument required but only 0 present.")
                return@setNonEnumerableNativeFunction JavascriptValue.Undefined
            }

            val matchingElement = htmlElement.domElementNode.getElementsWithId(id.toPrimitiveString())
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Null

            JavascriptValue.Object(JavascriptHtmlElement(interpreter, matchingElement))
        }

        setNonEnumerableNativeFunction("getElementsByTagName") { nativeExecutionContext ->
            val htmlElement = nativeExecutionContext.thisBinding as? JavascriptHtmlElement
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val tagName = nativeExecutionContext.arguments.firstOrNull()

            if (tagName == null) {
                nativeExecutionContext.interpreter.throwTypeError("Failed to execute 'getElementsByTagName': 1 argument required but only 0 present.")
                return@setNonEnumerableNativeFunction JavascriptValue.Undefined
            }

            val matchingElements = htmlElement.domElementNode
                .getElementsByTagName(tagName.toPrimitiveString())
                .map { JavascriptValue.Object(JavascriptHtmlElement(interpreter, it)) }

            JavascriptValue.Object(interpreter.makeArray(matchingElements))
        }

        setNonEnumerableNativeFunction("getElementsByClassName") { nativeExecutionContext ->
            val htmlElement = nativeExecutionContext.thisBinding as? JavascriptHtmlElement
                ?: return@setNonEnumerableNativeFunction JavascriptValue.Undefined

            val className = nativeExecutionContext.arguments.firstOrNull()

            if (className == null) {
                nativeExecutionContext.interpreter.throwTypeError("Failed to execute 'getElementsByClassName': 1 argument required but only 0 present.")
                return@setNonEnumerableNativeFunction JavascriptValue.Undefined
            }

            val matchingElements = htmlElement.domElementNode
                .getElementsByClassName(className.toPrimitiveString())
                .map { JavascriptValue.Object(JavascriptHtmlElement(interpreter, it)) }

            JavascriptValue.Object(interpreter.makeArray(matchingElements))
        }
    }
}
