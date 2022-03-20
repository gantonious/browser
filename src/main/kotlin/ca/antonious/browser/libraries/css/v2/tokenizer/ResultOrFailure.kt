package ca.antonious.browser.libraries.css.v2.tokenizer

sealed class ResultOrFailure<T> {
    data class Result<T>(val result: T): ResultOrFailure<T>()
    class Failure<T>: ResultOrFailure<T>()
}

fun <T> ResultOrFailure<T>.safeResult(): T? {
    return when (this) {
        is ResultOrFailure.Result -> result
        else -> null
    }
}

fun <T, R> ResultOrFailure<T>.mapResult(transform: (T) -> R): ResultOrFailure<R> {
    return when (this) {
        is ResultOrFailure.Result -> resultOf(transform(result))
        else -> failure()
    }
}

fun <T, R> ResultOrFailure<T>.flatMapResult(transform: (T) -> ResultOrFailure<R>): ResultOrFailure<R> {
    return when (this) {
        is ResultOrFailure.Result -> when (val transformResult = transform(this.result)) {
            is ResultOrFailure.Result -> resultOf(transformResult.result)
            else -> failure()
        }
        else -> failure()
    }
}

fun <T> failure(): ResultOrFailure<T> {
    return ResultOrFailure.Failure()
}

fun <T> resultOf(result: T): ResultOrFailure<T> {
    return ResultOrFailure.Result(result)
}