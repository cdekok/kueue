@file:Suppress("TooGenericExceptionThrown")

package eu.kueue.retry

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@kotlinx.coroutines.ExperimentalCoroutinesApi
class TimeoutRetryStrategyTest {

    @Test
    fun `test retry with timeout`() {
        runTest {
            val retry = 5
            var attempts = 0
            retryWithTimeOut(retries = retry) {
                attempts++
                throw Exception("oh no")
            }
            assertEquals(retry + 1, attempts)
        }
    }

    @Test
    fun `test 0 retries`() {
        runTest {
            var attempts = 0
            retryWithTimeOut(retries = 0) {
                attempts++
                throw Exception("oh no")
            }
            assertEquals(1, attempts)
        }
    }
}
