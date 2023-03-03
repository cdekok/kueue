package eu.kueue.example.pg.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import eu.kueue.Message
import eu.kueue.example.pg.*
import eu.kueue.example.pg.listener.IndexListener
import eu.kueue.example.pg.listener.RecordListener
import eu.kueue.pg.vertx.PgConsumer
import eu.kueue.subscribe
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class CommandConsumer : CliktCommand(
    name = "consumer",
    help = "Consume messages"
) {
    private val serializer: SerializerType by option(help = "Serializer type")
        .enum<SerializerType>()
        .default(SerializerType.KOTLINX)

    private val consumer by lazy {
        PgConsumer(
            client = pgPool(),
            serializer = serializer(serializer),
        )
    }

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
