@file:Suppress("TooGenericExceptionCaught")

package eu.kueue.retry

import eu.kueue.MessageProcessor
import eu.kueue.RetryPredicate
import eu.kueue.RetryStrategy
import kotlinx.coroutines.delay
import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TimeoutRetryStrategy<T>(
    private val predicate: RetryPredicate = {
        it is Exception
    },
    private val retries: Int = 5,
    private val timeout: Duration = 5.seconds,
) : RetryStrategy<T> {
    override suspend fun runWithRetry(message: T, processMessage: MessageProcessor<T>) = retryWithTimeOut(
        predicate = predicate,
        retries = retries,
        timeout = timeout,
        message = message,
    ) {
        processMessage(message)
    }
}

suspend inline fun <T> retryWithTimeOut(
    predicate: RetryPredicate = {
        it is Exception
    },
    retries: Int = 5,
    timeout: Duration = 5.seconds,
    message: T,
    processor: MessageProcessor<T>,
) {
    for (i in 0..retries) {
        return try {
            processor(message)
        } catch (e: Throwable) {
            when {
                !predicate(e) -> throw e
                i < retries -> {
                    val logger = KotlinLogging.logger { }
                    logger.error(e) { "retry attempt $i wait $timeout : $message" }
                    delay(timeout)

                    continue
                }

                else -> Unit
            }
        }
    }
}
