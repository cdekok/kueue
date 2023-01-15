package eu.kueue.pg.vertx

import eu.kueue.Message
import eu.kueue.MessageSerializer
import eu.kueue.Producer
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import java.util.*
import kotlin.reflect.KClass

class PgProducer(
    private val client: SqlClient,
    private val serializer: MessageSerializer
) : Producer {
    override suspend fun <T : Message> send(topic: String, message: T, clazz: KClass<T>) {
        val serializedMessage = serializer.serialize(message, clazz)

        val sql = """
            insert into kueue_messages(id, topic, message, class, created)
                values($1, $2, $3, $4, CURRENT_TIMESTAMP)
        """.trimIndent()

        val data = Tuple.of(
            UUID.randomUUID(),
            topic,
            serializedMessage,
            clazz.qualifiedName,
        )

        client.preparedQuery(sql).execute(data).await()
    }
}
