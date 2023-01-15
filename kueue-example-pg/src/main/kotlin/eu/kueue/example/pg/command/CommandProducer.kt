package eu.kueue.example.pg.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import eu.kueue.Producer
import eu.kueue.example.pg.DEFAULT_TOPIC
import eu.kueue.example.pg.kotlinXSerializer
import eu.kueue.example.pg.message.RecordCreated
import eu.kueue.example.pg.message.TestMessage
import eu.kueue.example.pg.pgPool
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

    @Suppress("MagicNumber")
    private val amount: Int by option(help = "Number of messages to add")
        .int()
        .default(10_000)

    private val producer: Producer = PgProducer(
        client = pgPool(),
        serializer = kotlinXSerializer(),
    )

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
                val message = listOf(
                    RecordCreated(
                        id = it,
                        title = "test title $it",
                    ),
                    TestMessage(
                        id = it,
                        title = "test title $it",
                    )
                ).random()

                producer.send(
                    topic = DEFAULT_TOPIC,
                    message = message,
                ).also {
                    logger.info { "send $message" }
                }
            }
        }
    }
}
