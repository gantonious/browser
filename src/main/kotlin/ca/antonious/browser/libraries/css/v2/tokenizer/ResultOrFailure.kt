package ca.antonious.browser.libraries.css.v2.tokenizer

sealed class ResultOrFailure<T> {
    data class Result<T>(val result: T): ResultOrFailure<T>()
    class Failure<T>: ResultOrFailure<T>()
}