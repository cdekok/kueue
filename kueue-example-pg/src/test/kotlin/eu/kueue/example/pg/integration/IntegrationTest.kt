package eu.kueue.example.pg.integration

import eu.kueue.example.pg.kotlinXSerializer
import eu.kueue.example.pg.message.TestMessage
import eu.kueue.pg.vertx.PgProducer
import eu.kueue.send
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

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
        val connection = PgConnectOptions().apply {
            database = PG_NAME
            user = PG_USER
            password = PG_PASS
            host = postgres.host
            port = postgres.getMappedPort(PG_PORT)
            reconnectAttempts = 10
            reconnectInterval = 5000
        }
        println(connection.database)
        PgPool.pool(
            connection,
            PoolOptions().apply {
                maxSize = PG_POOL
            }
        )
    }

    @Test
    @Order(1)
    fun `test producer send`() = runBlocking {
        val message = TestMessage(
            id = 1,
            title = "test"
        )

        val producer =
            PgProducer(
                client = pool,
                serializer = kotlinXSerializer(),
            )

        assertDoesNotThrow {
            producer.send(TOPIC, message)
        }
    }
}
