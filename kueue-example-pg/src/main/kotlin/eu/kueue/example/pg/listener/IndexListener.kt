package eu.kueue.example.pg.listener

import eu.kueue.EventHandler
import eu.kueue.EventListener
import eu.kueue.example.pg.message.IndexRecord
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class IndexListener : EventListener {
    @EventHandler
    fun on(event: IndexRecord) {
        logger.info { "handle index record $event" }
    }
}
