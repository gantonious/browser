package ca.antonious.browser.libraries.css.v2.tokenizer


class CssParser {
    private var topLevel = false

    fun parseACssStyleSheet(inputStream: CssTokenStream): CssStylesheet {
        val styleSheet = parseAStyleSheet(inputStream)

        return CssStylesheet(
            styleRules = styleSheet.rules.mapNotNull {
                if (it is CssRule.QualifiedRule) {
                    interpretAsStyleRule(it)
                } else {
                    null
                }
            }
        )
    }
    fun parseAStyleSheet(inputStream: CssTokenStream): Stylesheet {
        val stylesheet = Stylesheet(location = "null", rules = listOf())
        topLevel = true
        stylesheet.rules = consumeAListOfRules(inputStream)
        return stylesheet
    }

    private fun consumeAListOfRules(inputStream: CssTokenStream): List<CssRule> {
        val rules = mutableListOf<CssRule>()

        while (true) {
            when (val token = inputStream.consumeNextInputToken()) {
                CssTokenType.Whitespace -> continue
                CssTokenType.EndOfFile -> return rules
                CssTokenType.CDO,
                CssTokenType.CDC -> {
                    if (topLevel) {
                        continue
                    }

                    inputStream.reconsumeTheCurrentInputToken()
                    val qualifiedRule = consumeAQualifiedRule(inputStream)

                    if (qualifiedRule != null) {
                        rules.add(qualifiedRule)
                    }
                }
                is CssTokenType.AtKeyword -> {
                    inputStream.reconsumeTheCurrentInputToken()
                    consumeAnAtRule(inputStream)
                }
                else -> {
                    inputStream.reconsumeTheCurrentInputToken()
                    val qualifiedRule = consumeAQualifiedRule(inputStream)

                    if (qualifiedRule != null) {
                        rules.add(qualifiedRule)
                    }
                }
            }
        }
    }

    private fun consumeAnAtRule(inputStream: CssTokenStream): CssRule {
        val atRule = CssRule.AtRule(prelude = mutableListOf(), block = null)

        while (true) {
            when (val currentInputToken = inputStream.consumeNextInputToken()) {
                CssTokenType.SemiColon -> return atRule
                CssTokenType.EndOfFile -> {
                    raiseParseError()
                    return atRule
                }
                CssTokenType.LeftCurlyBracket -> {
                    atRule.block = consumeASimpleBlock(inputStream)
                    return atRule
                }
                else -> {
                    inputStream.reconsumeTheCurrentInputToken()
                    atRule.prelude.add(consumeAComponentValue(inputStream))
                }
            }
        }
    }

    private fun consumeAQualifiedRule(inputStream: CssTokenStream): CssRule? {
        val qualifiedRule = CssRule.QualifiedRule(prelude = mutableListOf(), block = null)
        while (true) {
            when (val currentInputToken = inputStream.consumeNextInputToken()) {
                CssTokenType.EndOfFile -> {
                    raiseParseError()
                    return null
                }
                CssTokenType.LeftCurlyBracket -> {
                    qualifiedRule.block = consumeASimpleBlock(inputStream)
                    return qualifiedRule
                }
                else -> {
                    inputStream.reconsumeTheCurrentInputToken()
                    qualifiedRule.prelude.add(consumeAComponentValue(inputStream))
                }
            }
        }
    }

    private fun consumeASimpleBlock(inputStream: CssTokenStream): SimpleBlock {
        val endingToken = when (inputStream.currentInputToken()) {
            CssTokenType.LeftCurlyBracket -> CssTokenType.RightCurlyBracket
            CssTokenType.LeftBracket -> CssTokenType.RightBracket
            CssTokenType.LeftParenthesis -> CssTokenType.RightParenthesis
            else -> error("Current ")
        }

        val simpleBlock = SimpleBlock(
            associatedToken = inputStream.currentInputToken() as CssTokenType,
            value = mutableListOf()
        )

        while (true) {
            when (val currentInputToken = inputStream.consumeNextInputToken()) {
                endingToken -> return simpleBlock
                CssTokenType.EndOfFile -> {
                    raiseParseError()
                    return simpleBlock
                }
                else -> {
                    inputStream.reconsumeTheCurrentInputToken()
                    simpleBlock.value.add(consumeAComponentValue(inputStream))
                }
            }
        }
    }

    private fun consumeAComponentValue(inputStream: CssTokenStream): ComponentValue {
        return when (val currentInputToken = inputStream.consumeNextInputToken()) {
            CssTokenType.LeftCurlyBracket,
            CssTokenType.LeftBracket,
            CssTokenType.LeftParenthesis -> {
                ComponentValue.Block(block = consumeASimpleBlock(inputStream))
            }
            is CssTokenType.Function -> {
                ComponentValue.Function(consumeAFunction(inputStream))
            }
            else -> when (currentInputToken) {
                is ComponentValue -> currentInputToken
                is CssTokenType -> ComponentValue.Token(currentInputToken)
                else -> error("Current input token must be a CSS token or component value.")
            }
        }
    }

    private fun consumeAFunction(inputStream: CssTokenStream): CssFunction {
        val function = CssFunction(name = inputStream.currentInputToken() as CssTokenType, value = mutableListOf())

        while (true) {
            when (val currentInputToken = inputStream.consumeNextInputToken()) {
                CssTokenType.LeftParenthesis -> return function
                CssTokenType.EndOfFile -> {
                    raiseParseError()
                    return function
                }
                else -> {
                    inputStream.reconsumeTheCurrentInputToken()
                    function.value.add(consumeAComponentValue(inputStream))
                }
            }
        }
    }

    private fun interpretAsStyleRule(qualifiedRule: CssRule.QualifiedRule): CssStyleRule? {
        val preludeTokenStream = CssTokenStream(input = ComponentValueStreamInput(qualifiedRule.prelude))
        val selectors = consumeSelectorList(inputStream = preludeTokenStream).safeResult() ?: return null

        val styleBlockContents = qualifiedRule.block?.let {
            val inputStream = CssTokenStream(input = ComponentValueStreamInput(it.value))
            consumeAStyleBlocksContents(inputStream)
        } ?: return null

        return CssStyleRule(
            selectors = selectors,
            contents = styleBlockContents
        )
    }

    private fun consumeSelectorList(inputStream: CssTokenStream): ResultOrFailure<List<CssSelector>> {
        return when (val compoundSelectorResult = consumeComplexSelector(inputStream)) {
            is ResultOrFailure.Result -> ResultOrFailure.Result(listOf(compoundSelectorResult.result))
            else -> ResultOrFailure.Failure()
        }
    }

    private fun consumeComplexSelector(inputStream: CssTokenStream): ResultOrFailure<CssSelector.Complex> {
        val rootSelector = when(val rootSelectorResult = consumeCompoundSelector(inputStream)) {
            is ResultOrFailure.Result -> rootSelectorResult.result
            else -> return ResultOrFailure.Failure()
        }

        val combinedSelectors = mutableListOf<CombinedSelector>()

        while (true) {
            when (inputStream.consumeNextInputToken()) {
                is CssTokenType.EndOfFile -> {
                    return ResultOrFailure.Result(
                        CssSelector.Complex(
                            rootSelector = rootSelector,
                            combinedSelectors = combinedSelectors
                        )
                    )
                }
                is CssTokenType.Whitespace -> {
                    val nextToken = inputStream.consumeNextInputToken()
                    inputStream.reconsumeTheCurrentInputToken()

                    if (nextToken is CssTokenType.EndOfFile) {
                        continue
                    }

                    combinedSelectors.add(
                        CombinedSelector(
                            combinator = SelectorCombinator.Descendant,
                            selector = when (val selectorResult = consumeCompoundSelector(inputStream)) {
                                is ResultOrFailure.Result -> selectorResult.result
                                else -> return ResultOrFailure.Failure()
                            }
                        )
                    )
                }
            }
        }
    }

    private fun consumeCompoundSelector(inputStream: CssTokenStream): ResultOrFailure<CssSelector.Compound> {
        return when (val simpleSelectorResult = consumeSimpleSelector(inputStream)) {
            is ResultOrFailure.Result -> ResultOrFailure.Result(CssSelector.Compound(listOf(simpleSelectorResult.result)))
            else -> ResultOrFailure.Failure()
        }
    }

    private fun consumeSimpleSelector(inputStream: CssTokenStream): ResultOrFailure<SimpleSelector> {
        inputStream.consumeWhiteSpace()

        return when (val currentInputToken = inputStream.consumeNextInputToken()) {
            is CssTokenType.HashToken -> ResultOrFailure.Result(SimpleSelector.Id(hash = currentInputToken))
            is CssTokenType.Ident -> {
                ResultOrFailure.Result(SimpleSelector.Type(currentInputToken))
            }
            is CssTokenType.Delim -> {
                when (currentInputToken.value) {
                    '.' -> {
                        val identToken = inputStream.consumeNextInputToken() as? CssTokenType.Ident ?: return ResultOrFailure.Failure()
                        ResultOrFailure.Result(SimpleSelector.Class(identToken))
                    }
                    else -> ResultOrFailure.Failure()
                }
            }
            else -> ResultOrFailure.Failure()
        }
    }

    private fun consumeAStyleBlocksContents(inputStream: CssTokenStream): List<StyleDeclaration> {
        val decls = mutableListOf<StyleDeclaration>()
        val rules = mutableListOf<CssRule>()

        while (true) {
            val currentInputToken = inputStream.consumeNextInputToken()
            when {
                currentInputToken is CssTokenType.Whitespace ||
                currentInputToken is CssTokenType.SemiColon -> continue
                currentInputToken is CssTokenType.EndOfFile -> {
                    return decls
                }
                currentInputToken is CssTokenType.AtKeyword -> {
                    inputStream.reconsumeTheCurrentInputToken()
                    rules += consumeAnAtRule(inputStream)
                }
                currentInputToken is CssTokenType.Ident -> {
                    val tempList = mutableListOf<ComponentValue>(ComponentValue.Token(inputStream.currentInputToken() as CssTokenType))

                    while (true) {
                        when (inputStream.nextInputToken()) {
                            is CssTokenType.SemiColon,
                            is CssTokenType.EndOfFile -> break
                        }

                        tempList += consumeAComponentValue(inputStream)
                    }

                    val declarationInputStream = CssTokenStream(input = ComponentValueStreamInput(tempList))
                    val declaration = consumeADeclaration(declarationInputStream)

                    if (declaration != null) {
                        decls += declaration
                    }
                }
                currentInputToken is CssTokenType.Delim && currentInputToken.value == '&' -> {
                    inputStream.reconsumeTheCurrentInputToken()
                    val qualifiedRule = consumeAQualifiedRule(inputStream)

                    if (qualifiedRule != null) {
                        rules += qualifiedRule
                    }
                }
                else -> {
                    raiseParseError()
                    inputStream.reconsumeTheCurrentInputToken()

                    when (inputStream.nextInputToken()) {
                        is CssTokenType.SemiColon,
                        is CssTokenType.EndOfFile -> Unit
                        else -> consumeAComponentValue(inputStream)
                    }
                }
            }
        }
    }

    private fun consumeADeclaration(inputStream: CssTokenStream): StyleDeclaration? {
        var indexOfLastNonWhitespaceToken = -1

        val declaration = StyleDeclaration(
            name = inputStream.consumeNextInputToken() as CssTokenType.Ident,
            value = mutableListOf(),
            important = false
        )

        while (inputStream.nextInputToken() is CssTokenType.Whitespace) {
            inputStream.consumeNextInputToken()
        }

        if (inputStream.nextInputToken() !is CssTokenType.Colon) {
            raiseParseError()
            return null
        }

        inputStream.consumeNextInputToken()

        while (inputStream.nextInputToken() is CssTokenType.Whitespace) {
            inputStream.consumeNextInputToken()
        }

        while (inputStream.nextInputToken() !is CssTokenType.EndOfFile) {
            val componentValue = consumeAComponentValue(inputStream)

            if (componentValue.maybeAsA<ComponentValue.Token>()?.tokenType !is CssTokenType.Whitespace) {
                indexOfLastNonWhitespaceToken = declaration.value.size
            }

            declaration.value += componentValue
        }

        if (
            indexOfLastNonWhitespaceToken - 1 >= 0 &&
            declaration.value.getOrNull(indexOfLastNonWhitespaceToken - 1)?.maybeAsA<ComponentValue.Token>()?.tokenType?.maybeAsA<CssTokenType.Delim>()?.value == '!' &&
            declaration.value.getOrNull(indexOfLastNonWhitespaceToken)?.maybeAsA<ComponentValue.Token>()?.tokenType?.maybeAsA<CssTokenType.Ident>()?.value?.lowercase() == "important"
        ) {
            declaration.important = true
            declaration.value.removeAt(indexOfLastNonWhitespaceToken - 1)
            declaration.value.removeAt(indexOfLastNonWhitespaceToken - 1)
        }

        if (declaration.value.lastOrNull()?.maybeAsA<ComponentValue.Token>()?.tokenType is CssTokenType.Whitespace) {
            declaration.value.removeLast()
        }

        return declaration
    }

    private fun raiseParseError() {

    }
}

inline fun <reified T> Any.maybeAsA(): T? {
    if (this is T) {
        return this
    }

    return null
}