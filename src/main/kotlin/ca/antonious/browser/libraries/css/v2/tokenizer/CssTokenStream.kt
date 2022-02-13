package ca.antonious.browser.libraries.css.v2.tokenizer

class CssTokenStream(private val input: CssTokenStreamInput) {
    private var reconsumeNextToken = false
    private var currentToken: Any? = null

    fun currentInputToken(): Any {
        return currentToken!!
    }

    fun nextInputToken(): Any {
        return consumeNextInputToken().also {
            reconsumeTheCurrentInputToken()
        }
    }

    fun consumeWhiteSpace() {
        consumeNextInputToken()

        if (currentInputToken() !is CssTokenType.Whitespace) {
            reconsumeTheCurrentInputToken()
        }
    }

    fun consumeNextInputToken(): Any {
        if (reconsumeNextToken) {
            reconsumeNextToken = false
            return currentInputToken()
        }

        return input.nextToken().also {
            currentToken = it
        }
    }

    fun reconsumeTheCurrentInputToken() {
        reconsumeNextToken = true
    }
}

interface CssTokenStreamInput {
    fun nextToken(): Any
}

class ComponentValueStreamInput(
    private val componentValues: List<ComponentValue>
): CssTokenStreamInput {
    private var cursor = 0

    override fun nextToken(): Any {
        if (cursor < componentValues.size) {
            return when (val value = componentValues[cursor++]) {
                is ComponentValue.Token -> value.tokenType
                else -> value
            }
        }

        return CssTokenType.EndOfFile
    }
}

class AnyStreamInput(
    private val values: List<Any>
): CssTokenStreamInput {
    private var cursor = 0

    override fun nextToken(): Any {
        if (cursor < values.size) {
            return values[cursor++]
        }

        return CssTokenType.EndOfFile
    }
}


class RawCssInputStream(
    private val source: String
): CssTokenStreamInput {
    private val tokenizer = CssTokenizer(source = source, filename = "")

    override fun nextToken(): Any {
        return tokenizer.consumeToken()
    }
}