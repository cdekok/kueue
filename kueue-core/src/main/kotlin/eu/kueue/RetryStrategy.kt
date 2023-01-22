package eu.kueue

typealias MessageProcessor<T> = suspend (message: T) -> Unit

typealias RetryPredicate = (cause: Throwable) -> Boolean

interface RetryStrategy<T> {
    suspend fun runWithRetry(
        message: T,
        processMessage: MessageProcessor<T>,
    )
}
