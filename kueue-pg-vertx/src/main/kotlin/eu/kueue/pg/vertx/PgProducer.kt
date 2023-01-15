package eu.kueue.pg.vertx

import com.github.f4b6a3.uuid.UuidCreator
import eu.kueue.Message
import eu.kueue.MessageSerializer
import eu.kueue.Producer
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.reflect.KClass

class PgProducer(
    private val client: SqlClient,
    private val serializer: MessageSerializer,
) : Producer {
    override suspend fun <T : Message> send(topic: String, message: T, clazz: KClass<T>) {
        val serializedMessage = serializer.serialize(message, clazz)

        val sql = """
            insert into kueue_messages(id, topic, message, class, created)
                values($1, $2, $3, $4, $5)
        """.trimIndent()

        val created = Instant.now()
        val id = UuidCreator.getTimeOrdered(
            created,
            null,
            null,
        )

        val data = Tuple.of(
            id,
            topic,
            serializedMessage,
            clazz.qualifiedName,
            OffsetDateTime.ofInstant(created, ZoneOffset.UTC),
        )

        client.preparedQuery(sql).execute(data).await()
    }
}
