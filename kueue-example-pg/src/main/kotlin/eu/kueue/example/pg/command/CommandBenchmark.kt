package eu.kueue.example.pg.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import eu.kueue.Message
import eu.kueue.Producer
import eu.kueue.example.pg.DEFAULT_TOPIC
import eu.kueue.example.pg.SerializerType
import eu.kueue.example.pg.listener.BatchCallbackListener
import eu.kueue.example.pg.listener.CallbackListener
import eu.kueue.example.pg.message.RecordCreated
import eu.kueue.example.pg.pgPool
import eu.kueue.example.pg.serializer
import eu.kueue.pg.vertx.PgConsumer
import eu.kueue.pg.vertx.PgProducer
import eu.kueue.send
import eu.kueue.subscribe
import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

private val logger = KotlinLogging.logger { }

enum class ListenerType {
    SINGLE,
    BATCH,
}

class CommandBenchmark : CliktCommand(name = "benchmark") {

    override fun help(context: Context): String = "Benchmark producing & consuming messages"

    private val amount: Int by option(help = "Number of messages to add")
        .int()
        .default(10_000)

    private val dispatcher = Dispatchers.Default

    private val serializer: SerializerType by option(help = "Serializer type")
        .enum<SerializerType>()
        .default(SerializerType.KOTLINX)

    private val type: ListenerType by option(help = "Type of listeners")
        .enum<ListenerType>()
        .default(ListenerType.SINGLE)

    private val producer: Producer by lazy {
        PgProducer(
            client = pgPool(),
            serializer = serializer(serializer),
        )
    }

    private val consumer by lazy {
        PgConsumer(
            client = pgPool(),
            serializer = serializer(serializer),
            pollRetryDelay = 0.seconds,
        )
    }

    override fun run() = runBlocking(dispatcher) {
        val producerDuration = measureTime {
            produceData()
        }

        val consumerDuration = measureTime {
            consumeData()
        }

        echo("Produced data in: $producerDuration")
        echo("Consumed data in: $consumerDuration")
        exitProcess(0)
    }

    private suspend fun produceData() = withContext(dispatcher) {
        val jobs = mutableListOf<Job>()
        repeat(amount) {
            launch {
                val message = RecordCreated(
                    id = it,
                    title = "test title $it",
                )
                producer.send(
                    topic = DEFAULT_TOPIC,
                    message = message,
                ).also {
                    logger.info { "send $message" }
                }
            }.also { jobs.add(it) }
        }
        jobs.joinAll()
    }

    private suspend fun consumeData() = withContext(dispatcher) {
        logger.info { "start consumer" }
        var count = 0

        val listener = when (type) {
            ListenerType.SINGLE -> CallbackListener {
                count++
                logger.info { "events received $count" }
                if (count == amount) {
                    consumer.stop()
                }
            }

            ListenerType.BATCH -> BatchCallbackListener { events ->
                count += events.size
                logger.info { "events received $count" }
                if (count == amount) {
                    consumer.stop()
                }
            }
        }

        consumer.subscribe<Message>(
            topic = DEFAULT_TOPIC,
            batchSize = 10,
            listeners = listOf(listener),
        )
        consumer.start()
    }
}
