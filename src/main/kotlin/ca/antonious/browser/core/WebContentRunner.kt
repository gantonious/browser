package ca.antonious.browser.core

import ca.antonious.browser.libraries.html.HtmlDocument
import ca.antonious.browser.libraries.html.HtmlElement
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptObject

class WebContentRunner(private val documentListener: (HtmlDocument) -> Unit) {

    private val document = HtmlDocument(
        root = HtmlElement.Node(
            name = "root",
            children = listOf(
                HtmlElement.Node(
                    name = "head",
                    children = listOf(
                        HtmlElement.Node(
                            name = "title",
                            children = listOf(
                                HtmlElement.Text("Title")
                            )
                        )
                    )
                ),
                HtmlElement.Node(
                    name = "body",
                    children = listOf(
                        HtmlElement.Node(
                            name = "h1",
                            attributes = mapOf(
                                "backgroundColor" to "red"
                            ),
                            children = listOf(
                                HtmlElement.Text("Heading")
                            )
                        ),
                        HtmlElement.Node(
                            name = "h1",
                            attributes = mapOf(
                                "backgroundColor" to "red",
                                "marginTop" to "20"
                            ),
                            children = listOf(
                                HtmlElement.Text("Heading2")
                            )
                        )
                    )
                )
            )
        )
    )

    private val javascriptInterpreter = JavascriptInterpreter().apply {
        globalObject.apply {
            setProperty("document", JavascriptValue.Object(
                value = JavascriptObject().apply {
                    // apply native functions for accessing document
                }
            ))
        }
    }

    init {
        documentListener.invoke(document)
    }
}