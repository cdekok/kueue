package eu.kueue

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

data class TestEvent(
    val id: Int,
) : Message

class TestListener : EventListener {
    @Suppress("UnusedPrivateMember")
    @EventHandler
    fun on(event: TestEvent) = Unit
}

class EventListenerTest {

    private val listeners = listOf(TestListener())

    @Test
    fun `test event listener first argument`() {
        val callables = listeners.eventHandlers()
        assertEquals(1, callables.size)
        assertEquals(callables.first().firstArgumentType, TestEvent::class)
    }
}
