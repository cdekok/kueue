package eu.kueue.example.pg.integration

import eu.kueue.*
import eu.kueue.example.pg.SerializerType
import eu.kueue.example.pg.message.RecordCreated
import eu.kueue.example.pg.message.RecordUpdated
import eu.kueue.example.pg.serializer
import eu.kueue.pg.vertx.PgConsumer
import eu.kueue.pg.vertx.PgProducer
import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.sqlclient.PoolOptions
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.concurrent.timer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val PG_USER = "test"
private const val PG_PASS = "t3st"
private const val PG_NAME = "kueue"
private const val PG_PORT = 5432
private const val PG_POOL = 10
private const val TOPIC = "record"

private fun postgresContainer(options: GenericContainer<*>.() -> Unit) =
    GenericContainer(
        DockerImageName
            .parse("postgres:15-alpine")
    ).apply(options)

private val logger = KotlinLogging.logger { }

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class IntegrationTest {
    companion object {
        @Container
        val postgres =
            postgresContainer {
                withExposedPorts(PG_PORT)
                withClasspathResourceMapping(
                    "db/",
                    "/docker-entrypoint-initdb.d/",
                    BindMode.READ_ONLY,
                )
                withEnv("POSTGRES_USER", PG_USER)
                withEnv("POSTGRES_PASSWORD", PG_PASS)
                withEnv("POSTGRES_DB", PG_NAME)
                waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 1))
            }
    }

    private val pool by lazy {
        PgBuilder.pool()
            .connectingTo(
                PgConnectOptions().apply {
                    database = PG_NAME
                    user = PG_USER
                    password = PG_PASS
                    host = postgres.host
                    port = postgres.getMappedPort(PG_PORT)
                    reconnectAttempts = 10
                    reconnectInterval = 5000
                }
            )
            .with(
                PoolOptions().apply {
                    maxSize = PG_POOL
                }
            ).build()
    }

    private val messageCount = 10

    private val serializer = serializer(
        type = SerializerType.KOTLINX
    )

    private val consumer = PgConsumer(
        client = pool,
        serializer = serializer,
    )

    @Test
    @Order(1)
    fun `test producer send`() = runBlocking {
        val producer =
            PgProducer(
                client = pool,
                serializer = serializer,
            )

        assertDoesNotThrow {
            repeat(messageCount) {
                val message = RecordUpdated(
                    id = it,
                    title = "test"
                )
                producer.send(TOPIC, message)
            }
        }
    }

    @Test
    @Order(2)
    fun `test consumer receive`() = runBlocking {
        val listener = CountListener(
            consumer = consumer,
            stopOnCount = messageCount,
        )
        consumer.subscribe<Message>(
            topic = TOPIC,
            batchSize = 8,
            listeners = listOf(listener)
        )
        consumeWithTimeout(consumer) {
            assertEquals(messageCount, listener.totalReceived)
        }
    }

    @Test
    @Order(3)
    fun `test consumer receive bulk`() = runBlocking {
        val sendMessageCount = 100
        val producer = PgProducer(
            client = pool,
            serializer = serializer,
        )

        assertDoesNotThrow {
            repeat(sendMessageCount) {
                val message = RecordCreated(
                    id = it,
                    title = "test $sendMessageCount"
                )
                producer.send(TOPIC, message)
            }
        }

        val listener = CountListener(
            consumer = consumer,
            stopOnCount = messageCount,
        )
        consumer.subscribe<Message>(
            topic = TOPIC,
            batchSize = 10,
            listeners = listOf(listener)
        )
        consumeWithTimeout(consumer) {
            assertEquals(sendMessageCount, listener.totalReceived)
            assertEquals(10, listener.listenerCalled)
        }
    }

    class CountListener(
        private val consumer: Consumer,
        private val stopOnCount: Int,
    ) : EventListener {
        var totalReceived = 0
        var listenerCalled = 0

        @EventHandler
        suspend fun on(event: RecordUpdated) {
            totalReceived++
            listenerCalled++
            logger.info { "received $event" }
            if (totalReceived == stopOnCount) {
                consumer.stop()
            }
        }

        @EventHandler
        suspend fun on(event: List<RecordCreated>) {
            totalReceived += event.size
            listenerCalled++
            logger.info { "received $event" }
            if (totalReceived == stopOnCount) {
                consumer.stop()
            }
        }
    }
}

suspend fun consumeWithTimeout(
    consumer: Consumer,
    timeout: Duration = 15.seconds,
    block: suspend () -> Unit
) =
    try {
        withTimeout(timeout) {
            consumer.start()
            block()
        }
    } catch (e: TimeoutCancellationException) {
        consumer.stop()
        throw e
    }
