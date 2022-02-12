package ca.antonious.browser.libraries.css.v2.tokenizer

import ca.antonious.browser.libraries.graphics.images.Result

class CssParser {
    private var topLevel = false

    fun parseAStyleSheet(inputStream: CssTokenStream): CssStylesheet {
        val stylesheet = CssStylesheet(location = "null", rules = listOf())
        topLevel = true
        stylesheet.rules = consumeAListOfRules(inputStream)

        stylesheet.rules.forEach {
            if (it is CssRule.QualifiedRule) {
                interpretAsStyleRule(it)
            }
        }
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
            else -> ComponentValue.Token(tokenType = currentInputToken as CssTokenType)
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

    private fun interpretAsStyleRule(qualifiedRule: CssRule.QualifiedRule) {
        val preludeTokenStream = CssTokenStream(input = ComponentValueStreamInput(qualifiedRule.prelude))
        val selector = consumeSelectorList(inputStream = preludeTokenStream)
        return
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

    private fun raiseParseError() {

    }
}