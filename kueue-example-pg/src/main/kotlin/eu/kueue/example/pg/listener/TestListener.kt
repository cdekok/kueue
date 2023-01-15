package eu.kueue.example.pg.listener

import eu.kueue.EventHandler
import eu.kueue.EventListener
import eu.kueue.example.pg.message.RecordCreated
import eu.kueue.example.pg.message.TestMessage
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class TestListener : EventListener {

    @EventHandler
    fun on(event: TestMessage) {
        logger.info { "handle test $event" }
    }

    @EventHandler
    fun on(event: RecordCreated) {
        logger.info { "handle record created $event" }
    }
}
