package eu.kueue.example.pg.listener

import eu.kueue.EventHandler
import eu.kueue.EventListener
import eu.kueue.Message
import eu.kueue.example.pg.message.RecordCreated
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class CallbackListener(
    private val onEvent: suspend (Message) -> Unit,
) : EventListener {

    @EventHandler
    suspend fun on(event: RecordCreated) {
        logger.info { "handle event: $event" }
        onEvent.invoke(event)
    }
}
