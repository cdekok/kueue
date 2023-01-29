package eu.kueue.example.pg.listener

import eu.kueue.EventHandler
import eu.kueue.EventListener
import eu.kueue.example.pg.message.RecordCreated
import eu.kueue.example.pg.message.RecordUpdated
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class RecordListener : EventListener {

    @EventHandler
    fun on(event: RecordUpdated) {
        logger.info { "handle test $event" }
    }

    @EventHandler
    fun on(event: RecordCreated) {
        logger.info { "handle record created $event" }
    }
}
