package ca.antonious.browser.libraries.javascript.lexer

import org.junit.Assert.assertEquals
import org.junit.Test

class LexerTests {

    @Test
    fun testEmptySource() {
        assert(
            source = "",
            produces = listOf()
        )
    }

    @Test
    fun testStringLiteral() {
        assert(
            source = "\"Test string\"",
            produces = listOf(
                JavascriptToken(
                    type = JavascriptTokenType.String,
                    value = "Test string",
                    sourceInfo = SourceInfo(0, 0)
                )
            )
        )
    }

    @Test
    fun testFunctionDeclaration() {
        assert(
            source = "function hello(var1, var2)",
            produces = listOf(
                JavascriptToken(
                    type = JavascriptTokenType.Function,
                    sourceInfo = SourceInfo(0, 0),
                    value = "function"
                ),
                JavascriptToken(
                    type = JavascriptTokenType.Identifier,
                    sourceInfo = SourceInfo(0, 0),
                    value = "hello"
                ),
                JavascriptToken(
                    type = JavascriptTokenType.OpenParentheses,
                    sourceInfo = SourceInfo(0, 0)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.Identifier,
                    sourceInfo = SourceInfo(0, 0),
                    value = "var1"
                ),
                JavascriptToken(
                    type = JavascriptTokenType.Comma,
                    sourceInfo = SourceInfo(0, 0)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.Identifier,
                    sourceInfo = SourceInfo(0, 0),
                    value = "var2"
                ),
                JavascriptToken(
                    type = JavascriptTokenType.CloseParentheses,
                    sourceInfo = SourceInfo(0, 0)
                )
            )
        )
    }

    private fun assert(source: String, produces: List<JavascriptToken>) {
        assertEquals(produces, Lexer(source.trimIndent()).lex())
    }
}