package eu.kueue.example.pg.listener

import eu.kueue.EventHandler
import eu.kueue.EventListener
import eu.kueue.Message
import eu.kueue.example.pg.message.RecordCreated
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class BatchCallbackListener(
    private val onEvent: suspend (List<Message>) -> Unit,
) : EventListener {

    @EventHandler
    suspend fun on(event: List<RecordCreated>) {
        logger.info { "handle events: ${event.size}" }
        onEvent.invoke(event)
    }
}
