package ca.antonious.browser.libraries.web.javascript

import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

val JavascriptInterpreter.elementPrototype: JavascriptObject
    get() {
        return globalObject.getProperty("Element").asFunction()!!.functionPrototype
    }
