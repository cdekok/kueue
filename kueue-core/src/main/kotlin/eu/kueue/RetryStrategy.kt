package eu.kueue

 fun interface RetryStrategy<T: Message> {
    suspend fun retry(cause: Throwable, message: T)
}
