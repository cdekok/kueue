package eu.kueue.serializer.kotlinx

import eu.kueue.Message
import eu.kueue.deserialize
import eu.kueue.serialize
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Serializable
data class TestMessage(
    val id: Int,
    val title: String,
) : Message

class KotlinxMessageSerializerTest {

    private val json = Json {
        classDiscriminator = "type"
        prettyPrint = true
        serializersModule = SerializersModule {
            polymorphic(Message::class) {
                subclass(TestMessage::class)
            }
        }
    }

    private val serde = KotlinxMessageSerializer(
        serde = json
    )

    private val message = TestMessage(
        id = 1,
        title = "test",
    )

    private val jsonString = """
            {
                "type": "eu.kueue.serializer.kotlinx.TestMessage",
                "id": 1,
                "title": "test"
            }
    """.trimIndent()

    @Test
    fun `serialize`() = runBlocking {
        val serialized = serde.serialize(message)
        assertEquals(jsonString, serialized)
    }

    @Test
    fun `deserialize`() = runBlocking {
        val deserialized: Message = serde.deserialize(jsonString)
        assertEquals(message, deserialized)
    }
}
