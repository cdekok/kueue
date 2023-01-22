@file:Suppress("TooGenericExceptionThrown")

package eu.kueue.retry

import eu.kueue.Message
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TimeoutRetryStrategyTest {

    data class TestMessage(
        val id: Int,
    ) : Message

    private val message = TestMessage(10)

    @Test
    fun `test retry with timeout`() {
        runTest {
            val retry = 5
            var attemps = 0
            retryWithTimeOut(
                message = message,
                retries = retry,
            ) {
                attemps++
                throw Exception("oh no")
            }
            assertEquals(retry + 1, attemps)
        }
    }

    @Test
    fun `test 0 retries`() {
        runTest {
            var attempts = 0
            retryWithTimeOut(
                message = message,
                retries = 0,
            ) {
                attempts++
                throw Exception("oh no")
            }
            assertEquals(1, attempts)
        }
    }
}
