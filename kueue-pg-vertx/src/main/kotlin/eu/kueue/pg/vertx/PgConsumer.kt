package eu.kueue.pg.vertx

import eu.kueue.*
import eu.kueue.retry.TimeoutRetryStrategy
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger { }

class PgConsumer(
    private val client: Pool,
    private val serializer: MessageSerializer,
    limitedParallelism: Int = 4,
    private val pollRetryDelay: Duration = 5.seconds,
    private val retryStrategy: RetryStrategy<Message> = TimeoutRetryStrategy(),
) : Consumer {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val dispatcher = Dispatchers.Default.limitedParallelism(limitedParallelism)

    private val subscriptions: MutableSet<Subscription<*>> = mutableSetOf()

    private var isActive = false

    override suspend fun <T : Message> subscribe(
        topic: String,
        batchSize: Int,
        listeners: List<EventListener>,
        clazz: KClass<T>,
    ) {
        val subscription = Subscription(
            topic = topic,
            batchSize = batchSize,
            listeners = listeners.eventHandlers(),
            clazz = clazz,
        )
        subscriptions.add(subscription)
    }

    /**
     * Process each subscription in order of added subscriptions
     */
    override suspend fun start() {
        isActive = true
        do {
            subscriptions.forEach { subscription ->
                logger.info { "consume ${subscription.topic}" }
                consumeAll(subscription)
            }
            logger.trace { "no jobs delay for $pollRetryDelay seconds" }
            delay(pollRetryDelay)
        } while (isActive)
    }

    /**
     * Stop consuming messages
     */
    override suspend fun stop() {
        isActive = false
    }

    /**
     * Consume all messages from subscription until there is nothing left
     */
    private suspend fun <T : Message> consumeAll(
        subscription: Subscription<T>,
    ) {
        logger.trace { "start processing: ${subscription.topic} amount: ${subscription.batchSize}" }
        var hasJobs = true
        do {
            withContext(dispatcher) {
                launch {
                    val jobs = pickJobs(subscription.topic, subscription.batchSize).map { row ->
                        serializer.deserialize(row.getString("message"), subscription.clazz)
                    }
                    logger.trace { "topic: ${subscription.topic} amount: ${subscription.batchSize} jobs: $jobs" }
                    if (jobs.isEmpty()) {
                        hasJobs = false
                    } else {
                        jobs.forEach { message ->
                            subscription.listeners.forEach { callable ->
                                if (callable.firstArgumentType == message::class) {
                                    retryStrategy.runWithRetry(
                                        message,
                                        callable::processMessage,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } while (hasJobs)
    }

    private suspend fun pickJobs(
        topic: String,
        limit: Int,
    ): RowSet<Row> {
        val query = """
            DELETE FROM kueue_messages
                WHERE id IN (
                  SELECT id FROM kueue_messages
                  WHERE topic = $1 
                  ORDER BY id ASC 
                  FOR UPDATE SKIP LOCKED
                  LIMIT $2
                )
                RETURNING id, message
        """.trimIndent()

        val params = Tuple.of(topic, limit)
        return client
            .transaction {
                preparedQuery(query).execute(params)
            }.coAwait()
    }
}

private data class Subscription<T : Message>(
    val topic: String,
    val batchSize: Int,
    val listeners: List<CallableListener>,
    val clazz: KClass<T>,
)

private suspend fun <R> Pool.transaction(code: suspend SqlConnection.() -> R): R {
    val connection = connection.coAwait()
    val transaction = connection.begin().coAwait()

    return try {
        code(connection).also {
            transaction.commit().coAwait()
        }
    } finally {
        connection.close()
    }
}
