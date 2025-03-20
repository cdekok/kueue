@file:Suppress("TooGenericExceptionCaught")

package eu.kueue.retry

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
    override suspend fun runWithRetry(action: suspend () -> T) = retryWithTimeOut(
        predicate = predicate,
        retries = retries,
        timeout = timeout,
    ) {
        action()
    }
}

@Suppress("ThrowsCount")
suspend inline fun <T> retryWithTimeOut(
    predicate: RetryPredicate = {
        it is Exception
    },
    retries: Int = 5,
    timeout: Duration = 5.seconds,
    action: () -> T,
): Result<T> {
    for (i in 0..retries) {
        return try {
            runCatching {
                action()
            }.onFailure {
                throw it
            }
        } catch (e: Throwable) {
            when {
                !predicate(e) -> throw e
                i < retries -> {
                    val logger = KotlinLogging.logger { }
                    logger.error(e) { "retry attempt $i wait $timeout" }
                    delay(timeout)

                    continue
                }

                else -> Result.failure(e)
            }
        }
    }
    error("Retry failure")
}
