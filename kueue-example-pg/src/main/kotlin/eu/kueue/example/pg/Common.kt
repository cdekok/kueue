package eu.kueue.example.pg

import eu.kueue.Message
import eu.kueue.example.pg.message.IndexRecord
import eu.kueue.example.pg.message.RecordCreated
import eu.kueue.example.pg.message.RecordUpdated
import eu.kueue.serializer.kotlinx.KotlinxMessageSerializer
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.lang.System.getenv

const val DEFAULT_TOPIC = "system"
const val INDEX_TOPIC = "index"

fun pgPool(): PgPool = PgPool.pool(
    PgConnectOptions().apply {
        database = getenv("PG_NAME")
        user = getenv("PG_USER")
        password = getenv("PG_PASS")
        port = getenv("PG_PORT")?.toInt() ?: 5432
        reconnectAttempts = 10
        reconnectInterval = 5000
    },
    PoolOptions().apply {
        maxSize = getenv("PG_POOL").toInt()
    },
)

fun kotlinXSerializer(): KotlinxMessageSerializer =
    KotlinxMessageSerializer(
        Json {
            classDiscriminator = "type"
            serializersModule = SerializersModule {
                polymorphic(Message::class) {
                    subclass(RecordUpdated::class)
                    subclass(RecordCreated::class)
                    subclass(IndexRecord::class)
                }
            }
        }
    )
