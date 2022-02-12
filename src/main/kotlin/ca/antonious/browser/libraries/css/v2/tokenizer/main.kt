package ca.antonious.browser.libraries.css.v2.tokenizer

fun main() {
    val css = """
        body {
        	background-color: #264653;
        }

        .square {
        	margin: 5px;
        	width: 50px;
        	height: 50px;
        	display: inline-block;
        }

        .square img {
        	width: 0px;
        	height: 100%;
        	vertical-align: middle;
        }

        .square span {
        	width: 100%;
        	text-align: center;
        	display: inline-block;
        	vertical-align: middle;
        }

        .empty-square {
        	background-color: #E9C46A;
        }

        .good-square {
        	background-color: #2A9D8F;
        }

        .bad-square {
        	background-color: #E76F51;
        }
    """.trimIndent()

    val tokenizer = CssTokenizer(source = css, filename = "")
    val tokens = mutableListOf<CssTokenType>()

    while (true) {
        val nextToken = tokenizer.consumeToken()

        if (nextToken is CssTokenType.EndOfFile) {
            break
        }

        tokens += nextToken
    }

    return
}