package eu.kueue.example.pg.command

import com.github.ajalt.clikt.core.CliktCommand
import eu.kueue.Message
import eu.kueue.example.pg.DEFAULT_TOPIC
import eu.kueue.example.pg.INDEX_TOPIC
import eu.kueue.example.pg.kotlinXSerializer
import eu.kueue.example.pg.listener.IndexListener
import eu.kueue.example.pg.listener.RecordListener
import eu.kueue.example.pg.pgPool
import eu.kueue.pg.vertx.PgConsumer
import eu.kueue.subscribe
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
        RecordListener()
    )

    override fun run() = runBlocking {
        logger.info { "start consumer" }
        consumer.subscribe<Message>(
            topic = DEFAULT_TOPIC,
            batchSize = 10,
            listeners = listeners,
        )
        consumer.subscribe<Message>(
            topic = INDEX_TOPIC,
            batchSize = 100,
            listeners = listOf(
                IndexListener()
            ),
        )
        consumer.start()
    }
}
