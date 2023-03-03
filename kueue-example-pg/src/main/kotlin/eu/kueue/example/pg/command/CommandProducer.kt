package eu.kueue.example.pg.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import eu.kueue.Producer
import eu.kueue.example.pg.*
import eu.kueue.example.pg.message.IndexRecord
import eu.kueue.example.pg.message.RecordCreated
import eu.kueue.example.pg.message.RecordUpdated
import eu.kueue.pg.vertx.PgProducer
import eu.kueue.send
import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private val logger = KotlinLogging.logger { }

class CommandProducer : CliktCommand(
    name = "producer",
    help = "Publish test messages"
) {
    private val amount: Int by option(help = "Number of messages to add")
        .int()
        .default(10_000)

    private val serializer: SerializerType by option(help = "Serializer type")
        .enum<SerializerType>()
        .default(SerializerType.KOTLINX)

    private val producer: Producer by lazy {
        PgProducer(
            client = pgPool(),
            serializer = serializer(serializer),
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = Dispatchers.Default.limitedParallelism(6)

    @OptIn(ExperimentalTime::class)
    override fun run() =
        runBlocking {
            val duration = measureTime {
                sendData()
            }
            logger.info { "done $amount messages published in ${duration.inWholeSeconds} seconds" }
        }.also {
            exitProcess(0)
        }

    private suspend fun sendData() = withContext(dispatcher) {
        repeat(amount) {
            async(dispatcher) {
                val (topic, message) = listOf(
                    DEFAULT_TOPIC to RecordCreated(
                        id = it,
                        title = "test title $it",
                    ),
                    DEFAULT_TOPIC to RecordUpdated(
                        id = it,
                        title = "test title $it",
                    ),
                    INDEX_TOPIC to IndexRecord(
                        id = it
                    ),
                ).random()

                producer.send(
                    topic = topic,
                    message = message,
                ).also {
                    logger.info { "send $message" }
                }
            }
        }
    }
}
