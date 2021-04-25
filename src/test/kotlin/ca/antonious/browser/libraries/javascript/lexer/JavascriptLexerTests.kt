package ca.antonious.browser.libraries.javascript.lexer

import ca.antonious.browser.libraries.shared.SourceInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class JavascriptLexerTests {

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
                    type = JavascriptTokenType.String("Test string"),
                    sourceInfo = SourceInfo(0, 0)
                )
            )
        )
    }

    @Test
    fun testFunctionDeclaration() {
        assert(
            source = """
                function hello(var1, var2) {
                    if (0xF > 10.5) {
                        return true
                    }
                    return "false"
                }
            """,
            produces = listOf(
                JavascriptToken(
                    type = JavascriptTokenType.Function,
                    sourceInfo = SourceInfo(0, 0)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.Identifier("hello"),
                    sourceInfo = SourceInfo(0, 9)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.OpenParentheses,
                    sourceInfo = SourceInfo(0, 14)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.Identifier("var1"),
                    sourceInfo = SourceInfo(0, 15)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.Comma,
                    sourceInfo = SourceInfo(0, 19)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.Identifier("var2"),
                    sourceInfo = SourceInfo(0, 21)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.CloseParentheses,
                    sourceInfo = SourceInfo(0, 25)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.OpenCurlyBracket,
                    sourceInfo = SourceInfo(0, 27)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.If,
                    sourceInfo = SourceInfo(1, 4)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.OpenParentheses,
                    sourceInfo = SourceInfo(1, 7)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.Number(15.0),
                    sourceInfo = SourceInfo(1, 8)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.Operator.GreaterThan,
                    sourceInfo = SourceInfo(1, 12)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.Number(10.5),
                    sourceInfo = SourceInfo(1, 14)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.CloseParentheses,
                    sourceInfo = SourceInfo(1, 18)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.OpenCurlyBracket,
                    sourceInfo = SourceInfo(1, 20)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.Return,
                    sourceInfo = SourceInfo(2, 8)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.Boolean(true),
                    sourceInfo = SourceInfo(2, 15)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.CloseCurlyBracket,
                    sourceInfo = SourceInfo(3, 4)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.Return,
                    sourceInfo = SourceInfo(4, 4)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.String("false"),
                    sourceInfo = SourceInfo(4, 11)
                ),
                JavascriptToken(
                    type = JavascriptTokenType.CloseCurlyBracket,
                    sourceInfo = SourceInfo(5, 0)
                )
            )
        )
    }

    private fun assert(source: String, produces: List<JavascriptToken>) {
        assertEquals(produces.toPrettyList(), JavascriptLexer(source.trimIndent()).lex().toPrettyList())
    }
}
