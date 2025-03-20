package eu.kueue

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventListenerTest {

    @Test
    fun `get parameter type of listener`() {
        val listener = SingleEventListener()
        assertEquals(RecordCreated::class, listener.eventHandlers().first().firstParameter.type())
    }

    @Test
    fun `get parameter type of batch listener`() {
        val listener = BatchEventListener()
        val handler = listener.eventHandlers().first()
        assertTrue {
            handler.firstParameter.isList()
        }

        assertEquals(RecordCreated::class, handler.firstParameter.listType())
    }
}

data class RecordCreated(
    val id: Int,
) : Message

class SingleEventListener : EventListener {
    @EventHandler
    suspend fun handle(event: RecordCreated) {
        println(event.id)
    }
}

class BatchEventListener : EventListener {
    @EventHandler
    suspend fun handle(event: List<RecordCreated>) {
        println(event)
    }
}
