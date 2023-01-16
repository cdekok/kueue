package eu.kueue.pg.vertx

import eu.kueue.*
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgPool
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
    private val client: PgPool,
    private val serializer: MessageSerializer,
    limitedParallelism: Int = 4,
    private val retryDelay: Duration = 5.seconds,
) : Consumer {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val dispatcher = Dispatchers.Default.limitedParallelism(limitedParallelism)

    private var isActive = false

    override suspend fun <T : Message> subscribe(
        topic: String,
        amount: Int,
        clazz: KClass<T>,
        callBack: ProcessMessage<T>,
    ) {
        if (isActive) {
            logger.trace { "listener is already active" }
            return
        } else {
            isActive = true
        }

        logger.trace { "start listening for topic: $topic amount: $amount" }

        while (isActive) {
            withContext(dispatcher) {
                launch {
                    val jobs = pickJobs(topic, amount).map { row ->
                        serializer.deserialize(row.getString("message"), clazz)
                    }
                    logger.trace { "topic: $topic amount: $amount jobs: $jobs" }
                    if (jobs.isEmpty()) {
                        logger.trace { "no jobs delay for $retryDelay seconds" }
                        delay(retryDelay)
                    } else {
                        callBack(jobs)
                    }
                }
            }
        }
    }

    override suspend fun subscribe(topic: String, amount: Int, listeners: List<EventListener>) {
        val callables = listeners.eventHandlers()
        subscribe(
            topic = topic,
            amount = amount,
        ) { messages ->
            messages.forEach { message ->
                callables.forEach { callable ->
                    val type = callable.firstArgumentType
                    if (type == message::class) {
                        callable.call(message)
                    }
                }
            }
        }
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
            }
            .await()
    }
}

private suspend fun <R> PgPool.transaction(code: suspend SqlConnection.() -> R): R {
    val connection = connection.await()
    val transaction = connection.begin().await()

    return try {
        code(connection).also {
            transaction.commit().await()
        }
    } finally {
        connection.close()
    }
}
