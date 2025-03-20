package eu.kueue

typealias RetryPredicate = (cause: Throwable) -> Boolean

fun interface RetryStrategy<T> {
    suspend fun runWithRetry(
        action: suspend () -> T
    ): Result<T>
}
