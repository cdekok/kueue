package eu.kueue.example.pg.command

import com.github.ajalt.clikt.core.CliktCommand
import eu.kueue.example.pg.DEFAULT_TOPIC
import eu.kueue.example.pg.kotlinXSerializer
import eu.kueue.example.pg.listener.TestListener
import eu.kueue.example.pg.pgPool
import eu.kueue.pg.vertx.PgConsumer
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class CommandConsumer : CliktCommand(
    name = "consumer",
    help = "Consume messages"
) {
    private val consumer = PgConsumer(
        client = pgPool(),
        serializer = kotlinXSerializer(),
    )

    private val listeners = listOf(
        TestListener()
    )

    override fun run() = runBlocking {
        logger.info { "start consumer" }
        consumer.subscribe(
            topic = DEFAULT_TOPIC,
            amount = 10,
            listeners = listeners,
        )
    }
}
